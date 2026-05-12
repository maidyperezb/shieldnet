package com.example.security.servicios;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.security.modelo.Protocolo;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NetworkTrafficService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkTrafficService.class);

    // Bytes acumulados por IP en la ventana actual
    private final ConcurrentHashMap<String, AtomicLong> bytesPerIp = new ConcurrentHashMap<>();

    // Velocidad calculada por IP (MB/s), snapshot del último segundo
    private volatile Map<String, Double> speedPerIp = new ConcurrentHashMap<>();

    // Snapshot anterior para el diferencial
    private final ConcurrentHashMap<String, Long> lastBytesPerIp = new ConcurrentHashMap<>();
    private final AtomicLong totalBytes = new AtomicLong(0);
    private final AtomicBoolean isCapturing = new AtomicBoolean(false);
    private final List<PcapHandle> activeHandles = new CopyOnWriteArrayList<>();

    private Thread captureThread;

    // Velocidad general
    private long lastBytes = 0;
    private long lastTime = System.currentTimeMillis();
    private double currentSpeed = 0.0;

    // Contadores por protocolo
    private final AtomicLong httpBytes = new AtomicLong(0);
    private final AtomicLong voipBytes = new AtomicLong(0);
    private final AtomicLong streamingBytes = new AtomicLong(0);
    private final AtomicLong otherBytes = new AtomicLong(0);

    private long lastHttpBytes = 0;
    private long lastVoipBytes = 0;
    private long lastStreamingBytes = 0;
    private long lastOtherBytes = 0;

    private Protocolo currentProtocolStats = new Protocolo();

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, String> connectionTypeMap = new ConcurrentHashMap<>();

    private final AtomicLong packetCounter = new AtomicLong(0);
    private static final long CLEANUP_INTERVAL = 10000;
    private static final int MAX_CONNECTIONS = 50000;

    // -------------------------------------------------------------------------
    // Inicio y parada de captura
    // -------------------------------------------------------------------------

    public void startCapturing(Consumer<Object> onUpdate) {
        if (isCapturing.compareAndSet(false, true)) {
            resetCounters();

            captureThread = new Thread(() -> {
                try {
                    List<PcapNetworkInterface> interfaces = Pcaps.findAllDevs();

                    System.out.println("Interfaces disponibles:");
                    for (int i = 0; i < interfaces.size(); i++) {
                        PcapNetworkInterface nif = interfaces.get(i);
                        System.out.println(i + ": " + nif.getName() + " - " + nif.getDescription());
                    }

                    // Seleccionar por descripción, no por índice fijo
                    List<PcapNetworkInterface> selectedInterfaces = interfaces.stream()
                        .filter(nif -> {
                            String desc = nif.getDescription() != null
                                          ? nif.getDescription().toLowerCase() : "";
                            String name = nif.getName().toLowerCase();

                            boolean isRealWifi     = desc.contains("realtek") && desc.contains("802.11");
                            boolean isRealEthernet = desc.contains("realtek") && desc.contains("gbe");
                            boolean isLoopback     = name.contains("loopback") || desc.contains("loopback");

                            boolean isVirtual = desc.contains("virtual")
                                             || desc.contains("hyper-v")
                                             || desc.contains("wi-fi direct")
                                             || desc.contains("vpn")
                                             || desc.contains("miniport");

                            return (isRealWifi || isRealEthernet || isLoopback) && !isVirtual;
                        })
                        .collect(java.util.stream.Collectors.toList());

                    // Fallback: si no encontró nada, usar las primeras interfaces con IPs
                    if (selectedInterfaces.isEmpty()) {
                        logger.warn("No se encontraron interfaces reales, usando fallback");
                        selectedInterfaces = interfaces.stream()
                            .filter(nif -> nif.getAddresses() != null
                                        && !nif.getAddresses().isEmpty())
                            .limit(2)
                            .collect(java.util.stream.Collectors.toList());
                    }

                    for (PcapNetworkInterface nif : selectedInterfaces) {
                        System.out.println("Iniciando captura en: " + nif.getName() + " - " + nif.getDescription());

                        // Modo no promiscuo para WiFi en Windows (evita bloqueos)
                        PcapNetworkInterface.PromiscuousMode mode =
                            nif.getDescription() != null && nif.getDescription().toLowerCase().contains("802.11")
                            ? PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS
                            : PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;

                        PcapHandle handle = nif.openLive(65536, mode, 10);
                        activeHandles.add(handle);

                        Thread captureInterfaceThread = new Thread(() -> {
                            try {
                                System.out.println(">>> Hilo de captura iniciado para: " + nif.getDescription());
                                handle.loop(-1, new PacketListener() {
                                    @Override
                                    public void gotPacket(Packet packet) {
                                        if (!isCapturing.get()) {
                                            try {
                                                handle.breakLoop();
                                            } catch (NotOpenException e) {
                                                logger.error("Error al detener captura: {}", e.getMessage());
                                            }
                                            return;
                                        }

                                        int packetSize = packet.length();
                                        long newTotal = totalBytes.addAndGet(packetSize);

                                        classifyPacket(packet, packetSize);

                                        long count = packetCounter.incrementAndGet();
                                        if (count % CLEANUP_INTERVAL == 0) {
                                            cleanupConnectionMap();
                                        }

                                        long currentTime = System.currentTimeMillis();
                                        long timeDiff = currentTime - lastTime;

                                        if (timeDiff >= 1000) {
                                            long bytesDiff = newTotal - lastBytes;
                                            currentSpeed = (bytesDiff / (1024.0 * 1024.0)) / (timeDiff / 1000.0);

                                            calculateProtocolSpeeds(timeDiff);
                                            calculateIpSpeeds(timeDiff);

                                            lastBytes = newTotal;
                                            lastTime = currentTime;

                                            broadcastTrafficData();

                                            System.out.println("Velocidad total: " + String.format("%.2f", currentSpeed) + " MB/s");
                                            System.out.println(currentProtocolStats.toString());
                                        }
                                    }
                                });
                            } catch (PcapNativeException | NotOpenException e) {
                                logger.error("Error en captura de interfaz: {}", e.getMessage());
                            } catch (InterruptedException e) {
                                logger.info("Captura interrumpida en: {}", nif.getName());
                            }
                        });

                        captureInterfaceThread.setDaemon(true);
                        captureInterfaceThread.start();
                    }

                } catch (PcapNativeException e) {
                    logger.error("Error nativo Pcap: {}", e.getMessage(), e);
                } catch (Exception e) {
                    logger.error("Error inesperado: {}", e.getMessage(), e);
                }
            });

            captureThread.setDaemon(true);
            captureThread.start();
        }
    }

    // -------------------------------------------------------------------------
    // Cálculo de velocidades por IP
    // -------------------------------------------------------------------------

    private void calculateIpSpeeds(long timeDiff) {
        double timeInSeconds = timeDiff / 1000.0;
        Map<String, Double> snapshot = new ConcurrentHashMap<>();

        for (Map.Entry<String, AtomicLong> entry : bytesPerIp.entrySet()) {
            String ip      = entry.getKey();
            long   current = entry.getValue().get();
            long   last    = lastBytesPerIp.getOrDefault(ip, 0L);
            long   diff    = current - last;

            double mbps = (diff / (1024.0 * 1024.0)) / timeInSeconds;
            if (mbps > 0.0001) {
                snapshot.put(ip, mbps);
            }

            lastBytesPerIp.put(ip, current);
        }

        lastBytesPerIp.keySet().retainAll(bytesPerIp.keySet());
        speedPerIp = snapshot;
    }

    // -------------------------------------------------------------------------
    // Parada y reset
    // -------------------------------------------------------------------------

    public void stopCapturing() {
        if (isCapturing.compareAndSet(true, false)) {
            cleanupHandles();

            if (captureThread != null) {
                captureThread.interrupt();
                captureThread = null;
            }

            currentSpeed = 0.0;
            currentProtocolStats = new Protocolo();
            broadcastTrafficData();
            logger.info("Captura de tráfico detenida");
        }
    }

    public void resetCounter() {
        resetCounters();
        broadcastTrafficData();
        logger.info("Contador de tráfico reiniciado");
    }

    // -------------------------------------------------------------------------
    // Clasificación de paquetes
    // -------------------------------------------------------------------------

    private void classifyPacket(Packet packet, int size) {
        IpV4Packet ipv4 = packet.get(IpV4Packet.class);
        if (ipv4 != null) {
            String src = ipv4.getHeader().getSrcAddr().getHostAddress();
            String dst = ipv4.getHeader().getDstAddr().getHostAddress();

            if (isLocalIp(src)) {
                bytesPerIp.computeIfAbsent(src, k -> new AtomicLong(0)).addAndGet(size);
            }
            if (isLocalIp(dst)) {
                bytesPerIp.computeIfAbsent(dst, k -> new AtomicLong(0)).addAndGet(size);
            }
        } else {
            IpV6Packet ipv6 = packet.get(IpV6Packet.class);
            if (ipv6 != null) {
                String src = ipv6.getHeader().getSrcAddr().getHostAddress();
                String dst = ipv6.getHeader().getDstAddr().getHostAddress();

                if (isLocalIpv6(src)) {
                    bytesPerIp.computeIfAbsent(src, k -> new AtomicLong(0)).addAndGet(size);
                }
                if (isLocalIpv6(dst)) {
                    bytesPerIp.computeIfAbsent(dst, k -> new AtomicLong(0)).addAndGet(size);
                }
            }
        }

        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        UdpPacket udpPacket = packet.get(UdpPacket.class);

        if (tcpPacket != null) {
            classifyTcpPacket(tcpPacket, size);
        } else if (udpPacket != null) {
            classifyUdpPacket(udpPacket, size);
        } else {
            otherBytes.addAndGet(size);
        }
    }

    private boolean isLocalIp(String ip) {
        return ip.startsWith("192.168.")
            || ip.startsWith("10.")
            || ip.startsWith("172.16.") || ip.startsWith("172.17.")
            || ip.startsWith("172.18.") || ip.startsWith("172.19.")
            || ip.startsWith("172.20.") || ip.startsWith("172.21.")
            || ip.startsWith("172.22.") || ip.startsWith("172.23.")
            || ip.startsWith("172.24.") || ip.startsWith("172.25.")
            || ip.startsWith("172.26.") || ip.startsWith("172.27.")
            || ip.startsWith("172.28.") || ip.startsWith("172.29.")
            || ip.startsWith("172.30.") || ip.startsWith("172.31.")
            || ip.equals("127.0.0.1");
    }

    private boolean isLocalIpv6(String ip) {
        String lower = ip.toLowerCase();
        return lower.startsWith("fe80:")
            || lower.startsWith("fc")
            || lower.startsWith("fd")
            || lower.equals("::1");
    }

    private void classifyTcpPacket(TcpPacket tcpPacket, int size) {
        int srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt();
        int dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();

        if (isStreamingPort(srcPort) || isStreamingPort(dstPort)) {
            streamingBytes.addAndGet(size);
            return;
        }

        if (isKnownStreamingAppPort(srcPort) || isKnownStreamingAppPort(dstPort)) {
            streamingBytes.addAndGet(size);
            return;
        }

        if (srcPort == 5060 || dstPort == 5060) {
            voipBytes.addAndGet(size);
            return;
        }

        if (isHttpPort(srcPort) || isHttpPort(dstPort)) {
            String connKey = buildConnectionKey(srcPort, dstPort);
            String knownType = connectionTypeMap.get(connKey);

            if (knownType != null) {
                applyProtocol(knownType, size);
                return;
            }

            Packet payload = tcpPacket.getPayload();
            if (payload != null) {
                String sni = extractSni(payload.getRawData());
                if (sni != null) {
                    String type = isStreamingSni(sni) ? "streaming" : "http";
                    connectionTypeMap.put(connKey, type);
                    applyProtocol(type, size);
                    logger.debug("SNI detectado: {} -> {}", sni, type);
                    return;
                }
            }

            httpBytes.addAndGet(size);
            return;
        }

        otherBytes.addAndGet(size);
    }

    private void classifyUdpPacket(UdpPacket udpPacket, int size) {
        int srcPort = udpPacket.getHeader().getSrcPort().valueAsInt();
        int dstPort = udpPacket.getHeader().getDstPort().valueAsInt();

        if (isVoipPort(srcPort) || isVoipPort(dstPort)) {
            voipBytes.addAndGet(size);
            return;
        }

        if (size < 300 && (srcPort >= 10000 || dstPort >= 10000)) {
            voipBytes.addAndGet(size);
            return;
        }

        if (isStreamingPort(srcPort) || isStreamingPort(dstPort)) {
            streamingBytes.addAndGet(size);
            return;
        }

        if (srcPort == 443 || dstPort == 443) {
            if (size > 100) {
                String connKey = "quic:" + Math.min(srcPort, dstPort) + "-" + Math.max(srcPort, dstPort);
                String knownType = connectionTypeMap.get(connKey);
                if (knownType != null) {
                    applyProtocol(knownType, size);
                } else {
                    httpBytes.addAndGet(size);
                }
            } else {
                httpBytes.addAndGet(size);
            }
            return;
        }

        if (srcPort == 53 || dstPort == 53 || srcPort == 123 || dstPort == 123) {
            otherBytes.addAndGet(size);
            return;
        }

        otherBytes.addAndGet(size);
    }

    // -------------------------------------------------------------------------
    // Helpers de clasificación
    // -------------------------------------------------------------------------

    private void applyProtocol(String type, int size) {
        switch (type) {
            case "streaming" -> streamingBytes.addAndGet(size);
            case "voip"      -> voipBytes.addAndGet(size);
            case "http"      -> httpBytes.addAndGet(size);
            default          -> otherBytes.addAndGet(size);
        }
    }

    private String buildConnectionKey(int srcPort, int dstPort) {
        int lo = Math.min(srcPort, dstPort);
        int hi = Math.max(srcPort, dstPort);
        return lo + "-" + hi;
    }

    private void cleanupConnectionMap() {
        if (connectionTypeMap.size() > MAX_CONNECTIONS) {
            connectionTypeMap.clear();
            logger.debug("Mapa de conexiones limpiado (superó {} entradas)", MAX_CONNECTIONS);
        }
    }

    private String extractSni(byte[] payload) {
        try {
            if (payload == null || payload.length < 43) return null;

            if (payload[0] != 0x16) return null;
            if (payload[5] != 0x01) return null;

            int pos = 43;
            if (pos >= payload.length) return null;

            int sessionIdLength = payload[pos] & 0xFF;
            pos += 1 + sessionIdLength;
            if (pos + 2 >= payload.length) return null;

            int cipherSuitesLength = ((payload[pos] & 0xFF) << 8) | (payload[pos + 1] & 0xFF);
            pos += 2 + cipherSuitesLength;
            if (pos + 1 >= payload.length) return null;

            int compressionMethodsLength = payload[pos] & 0xFF;
            pos += 1 + compressionMethodsLength;
            if (pos + 2 >= payload.length) return null;

            int extensionsLength = ((payload[pos] & 0xFF) << 8) | (payload[pos + 1] & 0xFF);
            pos += 2;
            int extensionsEnd = pos + extensionsLength;

            while (pos + 4 <= extensionsEnd && pos + 4 <= payload.length) {
                int extType   = ((payload[pos] & 0xFF) << 8) | (payload[pos + 1] & 0xFF);
                int extLength = ((payload[pos + 2] & 0xFF) << 8) | (payload[pos + 3] & 0xFF);
                pos += 4;

                if (extType == 0x0000 && pos + extLength <= payload.length) {
                    if (pos + 5 > payload.length) return null;
                    int nameLength = ((payload[pos + 3] & 0xFF) << 8) | (payload[pos + 4] & 0xFF);
                    int nameStart  = pos + 5;
                    if (nameStart + nameLength > payload.length) return null;
                    return new String(payload, nameStart, nameLength);
                }

                pos += extLength;
            }
        } catch (Exception e) {
            // Ignorar errores de parsing
        }
        return null;
    }

    private boolean isStreamingSni(String sni) {
        if (sni == null) return false;
        String s = sni.toLowerCase();
        return s.contains("googlevideo.com")
            || s.contains("youtube.com")
            || s.contains("ytimg.com")
            || s.contains("nflxvideo.net")
            || s.contains("nflximg.net")
            || s.contains("nflxext.com")
            || s.contains("netflix.com")
            || s.contains("fast.com")
            || s.contains("akamaihd.net")
            || s.contains("twitchsvc.net")
            || s.contains("twitch.tv")
            || s.contains("live-video.net")
            || s.contains("jtvnw.net")
            || s.contains("scdn.co")
            || s.contains("spotifycdn.com")
            || s.contains("spotify.com")
            || s.contains("aaplimg.com")
            || s.contains("mzstatic.com")
            || s.contains("disneyplus.com")
            || s.contains("dssott.com")
            || s.contains("bamgrid.com")
            || s.contains("hbomax.com")
            || s.contains("hbo.com")
            || s.contains("max.com")
            || s.contains("primevideo.com")
            || s.contains("amazonvideo.com")
            || s.contains("aiv-cdn.net")
            || s.contains("crunchyroll.com")
            || s.contains("vrv.co")
            || s.contains("plex.tv")
            || s.contains("plexapp.com");
    }

    // -------------------------------------------------------------------------
    // Helpers de puertos
    // -------------------------------------------------------------------------

    private boolean isHttpPort(int port) {
        return port == 80 || port == 443 || port == 8080 || port == 8443
            || port == 3000 || port == 8000 || port == 8888;
    }

    private boolean isVoipPort(int port) {
        return port == 5060 || port == 5061
            || (port >= 16384 && port <= 32767)
            || port == 3478 || port == 3479;
    }

    private boolean isStreamingPort(int port) {
        return port == 1935 || port == 554 || port == 8554 || port == 1755;
    }

    private boolean isKnownStreamingAppPort(int port) {
        return port == 4070 || port == 57621;
    }

    // -------------------------------------------------------------------------
    // Cálculo de velocidades por protocolo
    // -------------------------------------------------------------------------

    private void calculateProtocolSpeeds(long timeDiff) {
        long httpDiff      = httpBytes.get()      - lastHttpBytes;
        long voipDiff      = voipBytes.get()      - lastVoipBytes;
        long streamingDiff = streamingBytes.get() - lastStreamingBytes;
        long otherDiff     = otherBytes.get()     - lastOtherBytes;

        double timeInSeconds = timeDiff / 1000.0;

        currentProtocolStats.setHttpMbps(     (httpDiff      / (1024.0 * 1024.0)) / timeInSeconds);
        currentProtocolStats.setVoipMbps(     (voipDiff      / (1024.0 * 1024.0)) / timeInSeconds);
        currentProtocolStats.setStreamingMbps((streamingDiff / (1024.0 * 1024.0)) / timeInSeconds);
        currentProtocolStats.setOtherMbps(    (otherDiff     / (1024.0 * 1024.0)) / timeInSeconds);

        lastHttpBytes      = httpBytes.get();
        lastVoipBytes      = voipBytes.get();
        lastStreamingBytes = streamingBytes.get();
        lastOtherBytes     = otherBytes.get();
    }

    private void resetCounters() {
        lastBytes = 0;
        lastTime  = System.currentTimeMillis();
        currentSpeed = 0.0;
        totalBytes.set(0);

        httpBytes.set(0);
        voipBytes.set(0);
        streamingBytes.set(0);
        otherBytes.set(0);

        lastHttpBytes      = 0;
        lastVoipBytes      = 0;
        lastStreamingBytes = 0;
        lastOtherBytes     = 0;

        connectionTypeMap.clear();
        currentProtocolStats = new Protocolo();
        bytesPerIp.clear();
        lastBytesPerIp.clear();
        speedPerIp = new ConcurrentHashMap<>();
    }

    // -------------------------------------------------------------------------
    // SSE
    // -------------------------------------------------------------------------

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(   () -> emitters.remove(emitter));
        emitter.onError(  e  -> emitters.remove(emitter));

        try {
            TrafficData initialData = getCurrentTrafficData();
            String json = objectMapper.writeValueAsString(initialData);
            emitter.send(json);
            emitters.add(emitter);
        } catch (Exception e) {
            emitter.completeWithError(e);
            logger.error("Error al enviar datos iniciales al cliente SSE", e);
        }

        return emitter;
    }

    private void broadcastTrafficData() {
        try {
            TrafficData data = getCurrentTrafficData();
            String json = objectMapper.writeValueAsString(data);

            List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(json);
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                    logger.warn("Error al enviar actualización SSE", e);
                }
            }

            emitters.removeAll(deadEmitters);
        } catch (Exception e) {
            logger.error("Error al serializar datos de tráfico", e);
        }
    }

    public TrafficData getCurrentTrafficData() {
        return new TrafficData(currentSpeed, currentProtocolStats, speedPerIp);
    }

    // -------------------------------------------------------------------------
    // Utilidades
    // -------------------------------------------------------------------------

    private void cleanupHandles() {
        for (PcapHandle handle : activeHandles) {
            try {
                handle.breakLoop();
                handle.close();
            } catch (Exception e) {
                logger.warn("Error al cerrar handle de captura", e);
            }
        }
        activeHandles.clear();
    }

    public void printInterfaces() throws PcapNativeException {
        List<PcapNetworkInterface> interfaces = Pcaps.findAllDevs();
        for (int i = 0; i < interfaces.size(); i++) {
            System.out.println(i + ": " + interfaces.get(i).getName()
                + " - " + interfaces.get(i).getDescription());
        }
    }

    // -------------------------------------------------------------------------
    // Clases internas
    // -------------------------------------------------------------------------

    public static class TrafficData {
        private final double speed;
        private final Protocolo protocolo;
        private final Map<String, Double> speedPerIp;

        public TrafficData(double speed, Protocolo protocolStats, Map<String, Double> speedPerIp) {
            this.speed     = speed;
            this.protocolo = protocolStats;
            this.speedPerIp = speedPerIp;
        }

        public double getSpeed() { return speed; }
        public Protocolo getProtocolStats() { return protocolo; }
        public Map<String, Double> getSpeedPerIp() { return speedPerIp; }
    }

    // -------------------------------------------------------------------------
    // Ciclo de vida Spring
    // -------------------------------------------------------------------------

    @PostConstruct
    public void init() {
        logger.info("Servicio NetworkTraffic inicializado");
    }

    @PreDestroy
    public void cleanup() {
        stopCapturing();
        logger.info("Servicio NetworkTraffic detenido");
    }
}