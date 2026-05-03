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
import java.util.HashMap;

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

    /**
     * Mapa de conexiones TCP activas.
     * Clave: "srcIp:srcPort-dstIp:dstPort"
     * Valor: tipo de protocolo detectado ("streaming", "http", "voip", "other")
     */
    private final Map<String, String> connectionTypeMap = new ConcurrentHashMap<>();

    // Limpiar conexiones antiguas cada N paquetes
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

                    // Capturar de Wi-Fi (índice 4) y Loopback (índice 7)
                    List<Integer> interfacesToCapture = List.of(4, 7);

                    for (Integer index : interfacesToCapture) {
                        PcapNetworkInterface nif = interfaces.get(index);
                        System.out.println("Iniciando captura en: " + nif.getName() + " - " + nif.getDescription());

                        PcapHandle handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
                        activeHandles.add(handle);

                        Thread captureInterfaceThread = new Thread(() -> {
                            try {
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

                                        // Limpiar mapa de conexiones periódicamente
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

    private void calculateIpSpeeds(long timeDiff) {
    double timeInSeconds = timeDiff / 1000.0;
    Map<String, Double> snapshot = new ConcurrentHashMap<>();

    for (Map.Entry<String, AtomicLong> entry : bytesPerIp.entrySet()) {
        String ip      = entry.getKey();
        long   current = entry.getValue().get();
        long   last    = lastBytesPerIp.getOrDefault(ip, 0L);
        long   diff    = current - last;

        double mbps = (diff / (1024.0 * 1024.0)) / timeInSeconds;
        if (mbps > 0.0001) { // Filtrar IPs con tráfico insignificante
            snapshot.put(ip, mbps);
        }

        lastBytesPerIp.put(ip, current);
    }

    // Limpiar IPs que ya no tienen tráfico activo (no aparecieron en este ciclo)
    lastBytesPerIp.keySet().retainAll(bytesPerIp.keySet());

    speedPerIp = snapshot;
}

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

        // Solo contabilizar IPs de tu red local
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

            // IPv6 local: fe80:: (link-local) y fc00::/7 (unique local)
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

// IPv4 privadas: 192.168.x.x / 10.x.x.x / 172.16-31.x.x / loopback
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

// IPv6 locales: link-local (fe80::) y unique-local (fc:: / fd::)
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

        // Streaming tradicional (RTMP, RTSP)
        if (isStreamingPort(srcPort) || isStreamingPort(dstPort)) {
            streamingBytes.addAndGet(size);
            return;
        }

        // Apps de streaming conocidas por puerto
        if (isKnownStreamingAppPort(srcPort) || isKnownStreamingAppPort(dstPort)) {
            streamingBytes.addAndGet(size);
            return;
        }

        // VoIP SIP sobre TCP
        if (srcPort == 5060 || dstPort == 5060) {
            voipBytes.addAndGet(size);
            return;
        }

        // Tráfico HTTPS/HTTP
        if (isHttpPort(srcPort) || isHttpPort(dstPort)) {
            // Construir clave de conexión (bidireccional)
            String connKey = buildConnectionKey(srcPort, dstPort);

            // ¿Ya conocemos el tipo de esta conexión?
            String knownType = connectionTypeMap.get(connKey);

            if (knownType != null) {
                // Usar el tipo ya detectado para esta conexión
                applyProtocol(knownType, size);
                return;
            }

            // No conocemos esta conexión aún — intentar leer SNI del ClientHello
            Packet payload = tcpPacket.getPayload();
            if (payload != null) {
                String sni = extractSni(payload.getRawData());
                if (sni != null) {
                    // Tenemos SNI — clasificar y guardar para futuros paquetes
                    String type = isStreamingSni(sni) ? "streaming" : "http";
                    connectionTypeMap.put(connKey, type);
                    applyProtocol(type, size);
                    logger.debug("SNI detectado: {} -> {}", sni, type);
                    return;
                }
            }

            // Sin SNI (paquete de datos de conexión ya establecida sin clasificar)
            // Lo marcamos como http por defecto
            httpBytes.addAndGet(size);
            return;
        }

        otherBytes.addAndGet(size);
    }

    private void classifyUdpPacket(UdpPacket udpPacket, int size) {
        int srcPort = udpPacket.getHeader().getSrcPort().valueAsInt();
        int dstPort = udpPacket.getHeader().getDstPort().valueAsInt();

        // VoIP - RTP/RTCP
        if (isVoipPort(srcPort) || isVoipPort(dstPort)) {
            voipBytes.addAndGet(size);
            return;
        }

        // Heurística VoIP: paquetes pequeños en puertos altos
        if (size < 300 && (srcPort >= 10000 || dstPort >= 10000)) {
            voipBytes.addAndGet(size);
            return;
        }

        // Streaming UDP tradicional
        if (isStreamingPort(srcPort) || isStreamingPort(dstPort)) {
            streamingBytes.addAndGet(size);
            return;
        }

        // QUIC (UDP 443) - usado por YouTube, Netflix, etc.
        if (srcPort == 443 || dstPort == 443) {
            // QUIC también tiene SNI en el primer paquete (CRYPTO frame)
            // pero es más complejo de parsear; usamos heurística de tamaño
            // Paquetes QUIC iniciales (ClientHello) son grandes ~1200 bytes
            // Los de datos de video son también grandes y constantes
            if (size > 100) {
                // Intentar detectar por mapa de conexiones QUIC
                String connKey = "quic:" + Math.min(srcPort, dstPort) + "-" + Math.max(srcPort, dstPort);
                String knownType = connectionTypeMap.get(connKey);
                if (knownType != null) {
                    applyProtocol(knownType, size);
                } else {
                    // Sin info previa, clasificar como http (conservador)
                    httpBytes.addAndGet(size);
                }
            } else {
                httpBytes.addAndGet(size);
            }
            return;
        }

        // DNS, NTP
        if (srcPort == 53 || dstPort == 53 || srcPort == 123 || dstPort == 123) {
            otherBytes.addAndGet(size);
            return;
        }

        otherBytes.addAndGet(size);
    }

    // -------------------------------------------------------------------------
    // Helpers de clasificación
    // -------------------------------------------------------------------------

    /**
     * Aplica bytes al contador del protocolo indicado
     */
    private void applyProtocol(String type, int size) {
        switch (type) {
            case "streaming" -> streamingBytes.addAndGet(size);
            case "voip"      -> voipBytes.addAndGet(size);
            case "http"      -> httpBytes.addAndGet(size);
            default          -> otherBytes.addAndGet(size);
        }
    }

    /**
     * Clave bidireccional para identificar una conexión TCP
     * Ejemplo: "443-52341" (siempre el puerto menor primero)
     */
    private String buildConnectionKey(int srcPort, int dstPort) {
        int lo = Math.min(srcPort, dstPort);
        int hi = Math.max(srcPort, dstPort);
        return lo + "-" + hi;
    }

    /**
     * Limpia el mapa de conexiones si crece demasiado
     */
    private void cleanupConnectionMap() {
        if (connectionTypeMap.size() > MAX_CONNECTIONS) {
            connectionTypeMap.clear();
            logger.debug("Mapa de conexiones limpiado (superó {} entradas)", MAX_CONNECTIONS);
        }
    }

    /**
     * Extrae el SNI del ClientHello TLS.
     * El SNI va en texto claro aunque la conexión sea cifrada.
     */
    private String extractSni(byte[] payload) {
        try {
            if (payload == null || payload.length < 43) return null;

            // TLS Handshake = 0x16, ClientHello = 0x01
            if (payload[0] != 0x16) return null;
            if (payload[5] != 0x01) return null;

            int pos = 43;

            if (pos >= payload.length) return null;

            // Saltar Session ID
            int sessionIdLength = payload[pos] & 0xFF;
            pos += 1 + sessionIdLength;

            if (pos + 2 >= payload.length) return null;

            // Saltar Cipher Suites
            int cipherSuitesLength = ((payload[pos] & 0xFF) << 8) | (payload[pos + 1] & 0xFF);
            pos += 2 + cipherSuitesLength;

            if (pos + 1 >= payload.length) return null;

            // Saltar Compression Methods
            int compressionMethodsLength = payload[pos] & 0xFF;
            pos += 1 + compressionMethodsLength;

            if (pos + 2 >= payload.length) return null;

            // Leer Extensions
            int extensionsLength = ((payload[pos] & 0xFF) << 8) | (payload[pos + 1] & 0xFF);
            pos += 2;

            int extensionsEnd = pos + extensionsLength;

            while (pos + 4 <= extensionsEnd && pos + 4 <= payload.length) {
                int extType   = ((payload[pos] & 0xFF) << 8) | (payload[pos + 1] & 0xFF);
                int extLength = ((payload[pos + 2] & 0xFF) << 8) | (payload[pos + 3] & 0xFF);
                pos += 4;

                // Extensión SNI = tipo 0x0000
                if (extType == 0x0000 && pos + extLength <= payload.length) {
                    // pos+0,1 = SNI list length
                    // pos+2   = SNI type (0 = host_name)
                    // pos+3,4 = nombre length
                    // pos+5.. = nombre
                    if (pos + 5 > payload.length) return null;
                    int nameLength = ((payload[pos + 3] & 0xFF) << 8) | (payload[pos + 4] & 0xFF);
                    int nameStart  = pos + 5;
                    if (nameStart + nameLength > payload.length) return null;
                    return new String(payload, nameStart, nameLength);
                }

                pos += extLength;
            }

        } catch (Exception e) {
            // Ignorar errores de parsing — paquete malformado o fragmentado
        }
        return null;
    }

    /**
     * Determina si un hostname SNI corresponde a un servicio de streaming
     */
    private boolean isStreamingSni(String sni) {
        if (sni == null) return false;
        String s = sni.toLowerCase();
        return s.contains("googlevideo.com")   // YouTube
            || s.contains("youtube.com")       // YouTube
            || s.contains("ytimg.com")         // YouTube imágenes/chunks
            || s.contains("nflxvideo.net")     // Netflix video
            || s.contains("nflximg.net")       // Netflix imágenes
            || s.contains("nflxext.com")       // Netflix CDN
            || s.contains("netflix.com")       // Netflix
            || s.contains("fast.com")          // Netflix speedtest
            || s.contains("akamaihd.net")      // CDN usado por Twitch/otros
            || s.contains("twitchsvc.net")     // Twitch
            || s.contains("twitch.tv")         // Twitch
            || s.contains("live-video.net")    // Twitch live
            || s.contains("jtvnw.net")         // Twitch CDN
            || s.contains("scdn.co")           // Spotify CDN
            || s.contains("spotifycdn.com")    // Spotify
            || s.contains("spotify.com")       // Spotify
            || s.contains("aaplimg.com")       // Apple TV+
            || s.contains("mzstatic.com")      // Apple TV+
            || s.contains("disneyplus.com")    // Disney+
            || s.contains("dssott.com")        // Disney+ CDN
            || s.contains("bamgrid.com")       // Disney+ CDN
            || s.contains("hbomax.com")        // HBO Max
            || s.contains("hbo.com")           // HBO
            || s.contains("max.com")           // Max (HBO)
            || s.contains("primevideo.com")    // Amazon Prime Video
            || s.contains("amazonvideo.com")   // Amazon Prime Video
            || s.contains("aiv-cdn.net")       // Amazon Prime CDN
            || s.contains("crunchyroll.com")   // Crunchyroll
            || s.contains("vrv.co")            // VRV (Crunchyroll)
            || s.contains("plex.tv")           // Plex
            || s.contains("plexapp.com");      // Plex CDN
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
    // Cálculo de velocidades
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
        emitter.onTimeout(    () -> emitters.remove(emitter));
        emitter.onError(   e  -> emitters.remove(emitter));

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
            this.speed    = speed;
            this.protocolo = protocolStats;
            this.speedPerIp  = speedPerIp;
        }

        public double getSpeed() { return speed; }
        public Protocolo getProtocolStats() { return protocolo; }
         public Map<String, Double> getSpeedPerIp()  { return speedPerIp; } 
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