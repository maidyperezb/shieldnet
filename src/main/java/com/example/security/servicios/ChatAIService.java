package com.example.security.servicios;

import com.example.security.modelo.Dispositivo;
import com.example.security.modelo.EstadoAutorizacion;
import com.example.security.modelo.Evento;
import com.example.security.modelo.QoSMetrics;
import com.example.security.modelo.chat.EventHistoryRequest;
import com.example.security.modelo.chat.NetworkScanRequest;
import com.example.security.modelo.chat.QoSMetricsRequest;
import com.example.security.modelo.chat.ReportRequest;
import com.example.security.repositorio.DispositivoRepository;
import com.example.security.repositorio.EventoRepository;
import com.example.security.repositorio.QoSMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Servicio de integración con Spring AI y Groq. Define los 5 Tools disponibles
 * para Function Calling y configura el ChatClient.
 */
@Service
public class ChatAIService {

    private static final Logger logger = LoggerFactory.getLogger(ChatAIService.class);

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private QoSMetricsRepository qosRepository;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    // ============================================================================
    // TOOL 1: Query Event History
    // ============================================================================
    public Function<EventHistoryRequest, String> queryEventHistory() {
        return request -> {
            try {
                LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate()
                        : LocalDateTime.now().minusDays(7);
                LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate() : LocalDateTime.now();

                int maxResults = request.getMaxResults() != null ? request.getMaxResults() : 100;

                // Convertir a java.sql.Date para usar con EventoRepository
                java.sql.Date sqlStartDate = java.sql.Date.valueOf(startDate.toLocalDate());
                java.sql.Date sqlEndDate = java.sql.Date.valueOf(endDate.toLocalDate());

                List<Evento> eventos = eventoRepository.findByFechaBetween(sqlStartDate, sqlEndDate);

                // Limitar resultados
                if (eventos.size() > maxResults) {
                    eventos = eventos.subList(0, maxResults);
                }

                return formatEventosAsString(eventos);
            }
            catch (Exception e) {
                logger.error("Error en queryEventHistory", e);
                return "Error al consultar eventos: " + e.getMessage();
            }
        };
    }

    // ============================================================================
    // TOOL 2: Query QoS Metrics
    // ============================================================================
    public Function<QoSMetricsRequest, String> queryQoSMetrics() {
        return request -> {
            try {
                int days = request.getDays() != null ? request.getDays() : 7;
                LocalDateTime since = LocalDateTime.now().minusDays(days);

                List<QoSMetrics> metrics = qosRepository.findRecentMetrics(since);

                // Filtrar por protocolo si es especificado
                if (request.getProtocolType() != null && !request.getProtocolType().isEmpty()) {
                    metrics = metrics.stream()
                            .filter(m -> request.getProtocolType().equalsIgnoreCase(m.getProtocolType())).toList();
                }

                return formatQoSAsString(metrics);
            }
            catch (Exception e) {
                logger.error("Error en queryQoSMetrics", e);
                return "Error al consultar métricas QoS: " + e.getMessage();
            }
        };
    }

    // ============================================================================
    // TOOL 3: List Authorized Devices
    // ============================================================================
    public Function<Void, String> listAuthorizedDevices() {
        return unused -> {
            try {
                List<Dispositivo> allDevices = dispositivoRepository.findAll();
                List<Dispositivo> authorized = allDevices.stream()
                        .filter(d -> d.getEstadoAutorizacion() == EstadoAutorizacion.AUTORIZADO).toList();
                List<Dispositivo> unauthorized = allDevices.stream()
                        .filter(d -> d.getEstadoAutorizacion() == EstadoAutorizacion.NO_AUTORIZADO).toList();

                return formatDevicesAsString(authorized, unauthorized);
            }
            catch (Exception e) {
                logger.error("Error en listAuthorizedDevices", e);
                return "Error al listar dispositivos: " + e.getMessage();
            }
        };
    }

    // ============================================================================
    // TOOL 4: Execute Network Scan
    // ============================================================================
    public Function<NetworkScanRequest, String> executeNetworkScan() {
        return request -> {
            try {
                // Obtener información sobre dispositivos en la red
                List<Dispositivo> allDevices = dispositivoRepository.findAll();
                String type = request.getType() != null ? request.getType() : "device-list";
                String subnet = request.getSubnet() != null ? request.getSubnet() : "192.168.1.0/24";

                StringBuilder result = new StringBuilder();
                result.append(String.format("Escaneo de red solicitado: tipo=%s, subnet=%s\n", type, subnet));
                result.append(String.format("Dispositivos encontrados en la red: %d\n", allDevices.size()));

                if (!allDevices.isEmpty()) {
                    result.append("Detalles:\n");
                    allDevices.forEach(d -> result.append(String.format("  - IP: %s | MAC: %s | Estado: %s\n",
                            d.getIp(), d.getMac(), d.getEstadoAutorizacion())));
                }

                logger.info("NetworkScan Tool invocado: type={}, subnet={}", type, subnet);
                return result.toString();
            }
            catch (Exception e) {
                logger.error("Error en executeNetworkScan", e);
                return "Error al ejecutar escaneo de red: " + e.getMessage();
            }
        };
    }

    // ============================================================================
    // TOOL 5: Generate Monitoring Report
    // ============================================================================
    public Function<ReportRequest, String> generateMonitoringReport() {
        return request -> {
            try {
                int days = request.getDays() != null ? request.getDays() : 7;
                return generateReport(days);
            }
            catch (Exception e) {
                logger.error("Error en generateMonitoringReport", e);
                return "Error al generar reporte: " + e.getMessage();
            }
        };
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================

    /**
     * Retornar ChatClient configurado con Groq
     */
    @Bean
    public ChatClient getChatClient() {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Obtener lista de todas las herramientas disponibles
     */
    public List<Object> getAllTools() {
        return new ArrayList<>(); // Spring AI registra automáticamente desde @Bean
    }

    private String formatEventosAsString(List<Evento> eventos) {
        if (eventos.isEmpty()) {
            return "No hay eventos registrados.";
        }

        StringBuilder sb = new StringBuilder("Eventos encontrados:\n\n");
        for (Evento evento : eventos) {
            sb.append(String.format("• [%s %s] IP: %s\n  Descripción: %s\n  Usuario: %s\n\n", evento.getFecha(),
                    evento.getHora(), evento.getIp(), evento.getDescripcion(),
                    evento.getUsuario() != null ? evento.getUsuario().getUsername() : "Sistema"));
        }
        sb.append(String.format("Total: %d eventos%n", eventos.size()));
        return sb.toString();
    }

    private String formatQoSAsString(List<QoSMetrics> metrics) {
        if (metrics.isEmpty()) {
            return "No hay métricas QoS disponibles.";
        }

        StringBuilder sb = new StringBuilder("Métricas QoS encontradas:\n\n");

        // Calcular promedios
        double avgLatency = metrics.stream().mapToDouble(m -> m.getLatencyMs() != null ? m.getLatencyMs() : 0).average()
                .orElse(0);

        double avgJitter = metrics.stream().mapToDouble(m -> m.getJitterMs() != null ? m.getJitterMs() : 0).average()
                .orElse(0);

        double avgPacketLoss = metrics.stream()
                .mapToDouble(m -> m.getPacketLossPercent() != null ? m.getPacketLossPercent() : 0).average().orElse(0);

        double avgMos = metrics.stream().mapToDouble(m -> m.getMosScore() != null ? m.getMosScore() : 0).average()
                .orElse(0);

        sb.append(String.format("Promedios:\n"));
        sb.append(String.format("  • Latencia: %.2f ms (normal: < 100ms)\n", avgLatency));
        sb.append(String.format("  • Jitter: %.2f ms (normal: < 10ms)\n", avgJitter));
        sb.append(String.format("  • Pérdida de paquetes: %.2f %% (normal: < 1%%)\n", avgPacketLoss));
        sb.append(String.format("  • MOS Score: %.2f/5.0 (normal: > 3.5)\n\n", avgMos));

        // Mostrar últimas 10 métricas detalladas
        sb.append("Últimas 10 mediciones:\n");
        metrics.stream().limit(10).forEach(m -> {
            sb.append(String.format(
                    "  [%s] IP: %s | Latencia: %.2f ms | Jitter: %.2f ms | "
                            + "Pérdida: %.2f %% | MOS: %.2f | Protocolo: %s\n",
                    m.getTimestamp(), m.getTargetIp(), m.getLatencyMs() != null ? m.getLatencyMs() : 0,
                    m.getJitterMs() != null ? m.getJitterMs() : 0,
                    m.getPacketLossPercent() != null ? m.getPacketLossPercent() : 0,
                    m.getMosScore() != null ? m.getMosScore() : 0,
                    m.getProtocolType() != null ? m.getProtocolType() : "Unknown"));
        });

        return sb.toString();
    }

    private String formatDevicesAsString(List<Dispositivo> authorized, List<Dispositivo> unauthorized) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Resumen de Dispositivos:\n"));
        sb.append(String.format("  • Autorizados: %d\n", authorized.size()));
        sb.append(String.format("  • No autorizados: %d\n\n", unauthorized.size()));

        if (!authorized.isEmpty()) {
            sb.append("Dispositivos AUTORIZADOS:\n");
            authorized.forEach(d -> sb.append(String.format("  ✓ IP: %s | MAC: %s\n", d.getIp(), d.getMac())));
            sb.append("\n");
        }

        if (!unauthorized.isEmpty()) {
            sb.append("Dispositivos NO AUTORIZADOS:\n");
            unauthorized.forEach(d -> sb.append(String.format("IP: %s | MAC: %s\n", d.getIp(), d.getMac())));
        }
        else {
            sb.append("No hay dispositivos no autorizados en la red.\n");
        }

        return sb.toString();
    }

    private String generateReport(int days) {
        StringBuilder report = new StringBuilder();
        report.append(String.format("=== REPORTE DE MONITOREO (últimos %d días) ===\n\n", days));

        // Eventos
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        java.sql.Date sqlDate = java.sql.Date.valueOf(since.toLocalDate());
        List<Evento> eventos = eventoRepository.findByFechaBetween(sqlDate,
                new java.sql.Date(System.currentTimeMillis()));
        report.append(String.format("EVENTOS: %d registrados\n", eventos.size()));

        // QoS
        List<QoSMetrics> metrics = qosRepository.findRecentMetrics(since);
        double avgLatency = metrics.stream().mapToDouble(m -> m.getLatencyMs() != null ? m.getLatencyMs() : 0).average()
                .orElse(0);
        report.append(String.format("QoS: Latencia promedio %.2f ms\n", avgLatency));

        // Dispositivos
        List<Dispositivo> allDevices = dispositivoRepository.findAll();
        long authorized = allDevices.stream().filter(d -> d.getEstadoAutorizacion() == EstadoAutorizacion.AUTORIZADO)
                .count();
        long unauthorized = allDevices.stream()
                .filter(d -> d.getEstadoAutorizacion() == EstadoAutorizacion.NO_AUTORIZADO).count();
        report.append(String.format("DISPOSITIVOS: %d autorizados, %d no autorizados\n", authorized, unauthorized));

        report.append("\nRecomendaciones: Revisar eventos recientes y métricas QoS para optimizar red.\n");
        return report.toString();
    }
}
