package com.example.security.servicios;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.security.modelo.Dispositivo;
import com.example.security.modelo.EstadoAutorizacion;
import com.example.security.repositorio.DispositivoRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class ArpScannerService {

    private static final Logger logger = LoggerFactory.getLogger(ArpScannerService.class);

    @Autowired
    private DispositivoRepository dispositivoRepository;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private PcapHandle handle;
    private Thread scanThread;

    // SSE emitters para notificar al frontend en tiempo real
    private final List<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> emitters
            = new CopyOnWriteArrayList<>();

    // -------------------------------------------------------------------------
    // Inicio y parada
    // -------------------------------------------------------------------------

    @PostConstruct
    public void startOnBoot() {
        logger.info("Iniciando ARP Scanner automáticamente...");
        start();
    }

    @PreDestroy
    public void stopOnShutdown() {
        stop();
    }

    public void start() {
        if (isRunning.compareAndSet(false, true)) {
            scanThread = new Thread(this::runScan);
            scanThread.setDaemon(true);
            scanThread.start();
            logger.info("ARP Scanner iniciado");
        }
    }

    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            if (handle != null && handle.isOpen()) {
                try {
                    handle.breakLoop();
                    handle.close();
                } catch (Exception e) {
                    logger.warn("Error al cerrar handle ARP: {}", e.getMessage());
                }
            }
            if (scanThread != null) {
                scanThread.interrupt();
                scanThread = null;
            }
            logger.info("ARP Scanner detenido");
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    // -------------------------------------------------------------------------
    // Lógica principal de escaneo ARP
    // -------------------------------------------------------------------------

    private void runScan() {
    try {
        List<PcapNetworkInterface> interfaces = Pcaps.findAllDevs();

        if (interfaces.isEmpty()) {
            logger.error("No se encontraron interfaces de red");
            return;
        }

        // ✅ Buscar la interfaz real, no usar índice fijo
        PcapNetworkInterface nif = findRealInterface(interfaces);

        if (nif == null) {
            logger.error("No se encontró interfaz de red real (no virtual)");
            return;
        }

        logger.info("ARP Scanner escuchando en: {} - {}", nif.getName(), nif.getDescription());

        handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        handle.setFilter("arp", BpfProgram.BpfCompileMode.OPTIMIZE);

        handle.loop(-1, (PacketListener) packet -> {
            if (!isRunning.get()) {
                try { handle.breakLoop(); } catch (NotOpenException e) { /* ignorar */ }
                return;
            }
            processArpPacket(packet);
        });

    } catch (InterruptedException e) {
        logger.info("ARP Scanner interrumpido");
    } catch (PcapNativeException | NotOpenException e) {
        logger.error("Error en ARP Scanner: {}", e.getMessage(), e);
    }
}

/**
 * Encuentra la primera interfaz real (no virtual, no loopback)
 */
private PcapNetworkInterface findRealInterface(List<PcapNetworkInterface> interfaces) {
    for (PcapNetworkInterface nif : interfaces) {
        String desc = nif.getDescription() != null ? nif.getDescription().toLowerCase() : "";
        String name = nif.getName().toLowerCase();

        // Saltar virtuales
        if (desc.contains("virtualbox") || desc.contains("vmware") || desc.contains("virtual")
         || desc.contains("loopback")   || desc.contains("bluetooth")
         || name.contains("vbox")       || name.contains("docker")
         || name.contains("virbr")      || name.contains("lo")) {
            logger.debug("Saltando interfaz virtual: {}", desc);
            continue;
        }

        // Verificar que tenga IP privada real
        for (PcapAddress addr : nif.getAddresses()) {
            if (addr.getAddress() instanceof java.net.Inet4Address) {
                String ip = addr.getAddress().getHostAddress();
                if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.20.")) {
                    logger.info("Interfaz real seleccionada: {} [{}] IP={}", nif.getName(), desc, ip);
                    return nif;
                }
            }
        }
    }
    return null;
}


    @Autowired
    private MacAutorizadaService macAutorizadaService; 

    /**
     * Estado de autorización de una MAC vista por el escáner ARP. Se extrae en
     * un método propio para poder verificarlo sin ejecutar la captura real.
     * Una MAC inactiva nunca vuelve a autorizarse; queda NO_AUTORIZADO y no se
     * confunde con un dispositivo desconocido.
     */
    EstadoAutorizacion determinarEstadoAutorizacion(String mac) {
        if (macAutorizadaService.esMacAutorizada(mac)) {
            return EstadoAutorizacion.AUTORIZADO;
        }
        if (macAutorizadaService.existeMac(mac)) {
            return EstadoAutorizacion.NO_AUTORIZADO;
        }
        return EstadoAutorizacion.DESCONOCIDO;
    }

    private void processArpPacket(Packet packet) {
    try {
        ArpPacket arpPacket = packet.get(ArpPacket.class);
        if (arpPacket == null) return;

        ArpPacket.ArpHeader header = arpPacket.getHeader();

        if (!header.getOperation().equals(ArpOperation.REQUEST) &&
            !header.getOperation().equals(ArpOperation.REPLY)) {
            return;
        }

        String senderIp  = header.getSrcProtocolAddr().getHostAddress();
        // Leer MAC del remitente
        byte[] macBytes  = header.getSrcHardwareAddr().getAddress();
        String senderMac = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                macBytes[0], macBytes[1], macBytes[2],
                macBytes[3], macBytes[4], macBytes[5]);

        if (senderIp == null || senderIp.isEmpty()
                || senderIp.equals("0.0.0.0")
                || senderIp.startsWith("169.254")) {
            return;
        }

        // Determinar estado según MAC:
        //  - MAC activa en la lista       -> AUTORIZADO
        //  - MAC registrada pero inactiva -> NO_AUTORIZADO (revocada)
        //  - MAC no registrada            -> DESCONOCIDO
        EstadoAutorizacion estado = determinarEstadoAutorizacion(senderMac);

        Dispositivo existente = dispositivoRepository.findByIp(senderIp);

        if (existente == null) {
            // Dispositivo nuevo
            Dispositivo nuevo = new Dispositivo();
            nuevo.setIp(senderIp);
            nuevo.setMac(senderMac);
            nuevo.setUltimaConexion(new Date());
            nuevo.setEstadoAutorizacion(estado);
            Dispositivo guardado = dispositivoRepository.save(nuevo);
            logger.info("Nuevo dispositivo: IP={} MAC={} Estado={}", senderIp, senderMac, estado);
            notifyNewDevice(guardado);
        } else {
            // Actualizar MAC y estado si cambió
            existente.setMac(senderMac);
            existente.setUltimaConexion(new Date());
            existente.setEstadoAutorizacion(estado);
            dispositivoRepository.save(existente);
        }

    } catch (Exception e) {
        logger.error("Error procesando paquete ARP: {}", e.getMessage());
    }
}

    // -------------------------------------------------------------------------
    // SSE para notificar nuevos dispositivos al frontend en tiempo real
    // -------------------------------------------------------------------------

    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribe() {
        var emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(()    -> emitters.remove(emitter));
        emitter.onError(e       -> emitters.remove(emitter));

        emitters.add(emitter);
        return emitter;
    }

    private void notifyNewDevice(Dispositivo dispositivo) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();

            // Construir payload simple
            String json = mapper.writeValueAsString(new DeviceNotification(
                    dispositivo.getIp(),
                    dispositivo.getEstadoAutorizacion().name(),
                    dispositivo.getUltimaConexion()
            ));

            List<org.springframework.web.servlet.mvc.method.annotation.SseEmitter> dead
                    = new CopyOnWriteArrayList<>();

            for (var emitter : emitters) {
                try {
                    emitter.send(json);
                } catch (Exception e) {
                    dead.add(emitter);
                }
            }

            emitters.removeAll(dead);

        } catch (Exception e) {
            logger.error("Error notificando nuevo dispositivo: {}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Clase interna para la notificación SSE
    // -------------------------------------------------------------------------

    public static class DeviceNotification {
        private final String ip;
        private final String estadoAutorizacion;
        private final Date ultimaConexion;

        public DeviceNotification(String ip, String estadoAutorizacion, Date ultimaConexion) {
            this.ip = ip;
            this.estadoAutorizacion = estadoAutorizacion;
            this.ultimaConexion = ultimaConexion;
        }

        public String getIp() { return ip; }
        public String getEstadoAutorizacion() { return estadoAutorizacion; }
        public Date getUltimaConexion() { return ultimaConexion; }
    }

    /**
 * Envía paquetes ARP a toda la subred para forzar que los dispositivos respondan
 */
public void scanNetwork() {
    Thread scanThread = new Thread(() -> {
        try {
            String subnet = detectRealSubnet();
            
            if (subnet == null) {
                logger.error("No se pudo detectar la subred real");
                return;
            }

            logger.info("Iniciando escaneo manual de subred: {}*", subnet);

            for (int i = 1; i <= 254; i++) {
                String ip = subnet + i;
                try {
                    java.net.InetAddress address = java.net.InetAddress.getByName(ip);
                    address.isReachable(100);
                } catch (Exception e) {
                    // ignorar
                }
            }

            logger.info("Escaneo manual completado");

        } catch (Exception e) {
            logger.error("Error en escaneo manual: {}", e.getMessage());
        }
    });

    scanThread.setDaemon(true);
    scanThread.start();
}

/**
 * Detecta la subred real ignorando interfaces virtuales (VirtualBox, VMware, Docker)
 */
private String detectRealSubnet() {
    try {
        java.util.Enumeration<java.net.NetworkInterface> interfaces =
                java.net.NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            java.net.NetworkInterface iface = interfaces.nextElement();

            // Saltar inactivas, loopback y virtuales
            if (!iface.isUp() || iface.isLoopback()) continue;

            String name        = iface.getName().toLowerCase();
            String displayName = iface.getDisplayName().toLowerCase();

            if (name.contains("vbox")     || name.contains("vmware")
             || name.contains("virtual")  || name.contains("docker")
             || name.contains("veth")     || name.contains("virbr")
             || displayName.contains("virtualbox")
             || displayName.contains("vmware")
             || displayName.contains("virtual")) {
                logger.debug("Saltando interfaz virtual: {}", displayName);
                continue;
            }

            // Buscar IPv4 privada en esta interfaz
            java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                java.net.InetAddress addr = addresses.nextElement();

                if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                    String ip = addr.getHostAddress();

                    // Solo rangos privados reales
                    if (ip.startsWith("192.168.")
                     || ip.startsWith("10.")
                     || ip.startsWith("172.")) {

                        String subnet = ip.substring(0, ip.lastIndexOf('.') + 1);
                        logger.info("Subred real detectada: {} (interfaz: {})", subnet, displayName);
                        return subnet;
                    }
                }
            }
        }
    } catch (Exception e) {
        logger.error("Error detectando subred: {}", e.getMessage());
    }

    return null; // No se encontró — no hacer fallback incorrecto
}
}