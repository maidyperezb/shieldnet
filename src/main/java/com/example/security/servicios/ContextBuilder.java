package com.example.security.servicios;

import com.example.security.modelo.Dispositivo;
import com.example.security.modelo.EstadoAutorizacion;
import com.example.security.modelo.Evento;
import com.example.security.modelo.QoSMetrics;
import com.example.security.modelo.Usuario;
import com.example.security.repositorio.DispositivoRepository;
import com.example.security.repositorio.EventoRepository;
import com.example.security.repositorio.QoSMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Servicio para construir el contexto dinámico que se pasa al modelo de IA.
 * Consolida datos de eventos, QoS y dispositivos en un formato legible para
 * Groq.
 */
@Service
public class ContextBuilder {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private QoSMetricsRepository qosRepository;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${chat.context-days:7}")
    private int contextDays;

    /**
     * Construir contexto completo para la sesión del usuario
     */
    public String buildFullContext(Usuario usuario) {
        StringBuilder context = new StringBuilder();

        // 1. Schema/descripción de tablas
        context.append("## ESQUEMA DE DATOS DISPONIBLES\n");
        context.append(getSchemaDescription());

        // 2. Eventos recientes
        context.append("\n## EVENTOS HISTÓRICOS (últimos ").append(contextDays).append(" días)\n");
        context.append(buildEventContext());

        // 3. Métricas QoS
        context.append("\n## MÉTRICAS QoS (últimos ").append(contextDays).append(" días)\n");
        context.append(buildQoSContext());

        // 4. Dispositivos
        context.append("\n## DISPOSITIVOS EN RED\n");
        context.append(buildDeviceContext());

        return context.toString();
    }

    /**
     * Construir contexto de eventos recientes
     */
    public String buildEventContext() {
        LocalDate startDate = LocalDate.now().minusDays(contextDays);
        LocalDate endDate = LocalDate.now();

        List<Evento> eventos = eventoRepository.findByFechaBetween(java.sql.Date.valueOf(startDate),
                java.sql.Date.valueOf(endDate));

        StringBuilder sb = new StringBuilder();

        if (eventos.isEmpty()) {
            sb.append("No hay eventos registrados en los últimos ").append(contextDays).append(" días.\n");
        }
        else {
            for (Evento evento : eventos) {
                sb.append(String.format("• [%s %s] IP: %s - %s (Usuario: %s)%n", evento.getFecha(), evento.getHora(),
                        evento.getIp(), evento.getDescripcion(),
                        evento.getUsuario() != null ? evento.getUsuario().getUsername() : "Sistema"));
            }
            sb.append(String.format("\nTotal de eventos: %d%n", eventos.size()));
        }

        return sb.toString();
    }

    /**
     * Construir contexto de métricas QoS
     */
    public String buildQoSContext() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(contextDays);
        List<QoSMetrics> metrics = qosRepository.findRecentMetrics(startTime);

        StringBuilder sb = new StringBuilder();

        if (metrics.isEmpty()) {
            sb.append("No hay métricas QoS disponibles en los últimos ").append(contextDays).append(" días.\n");
            return sb.toString();
        }

        // Calcular promedios
        OptionalDouble avgLatency = metrics.stream().mapToDouble(m -> m.getLatencyMs() != null ? m.getLatencyMs() : 0)
                .average();

        OptionalDouble avgJitter = metrics.stream().mapToDouble(m -> m.getJitterMs() != null ? m.getJitterMs() : 0)
                .average();

        OptionalDouble avgPacketLoss = metrics.stream()
                .mapToDouble(m -> m.getPacketLossPercent() != null ? m.getPacketLossPercent() : 0).average();

        OptionalDouble avgMos = metrics.stream().mapToDouble(m -> m.getMosScore() != null ? m.getMosScore() : 0)
                .average();

        sb.append(String.format("Promedios últimos %d días:%n", contextDays));
        sb.append(String.format("  • Latencia: %.2f ms%n", avgLatency.orElse(0)));
        sb.append(String.format("  • Jitter: %.2f ms%n", avgJitter.orElse(0)));
        sb.append(String.format("  • Pérdida de paquetes: %.2f %%%n", avgPacketLoss.orElse(0)));
        sb.append(String.format("  • MOS Score (calidad voz): %.2f/5.0%n", avgMos.orElse(0)));

        // Evaluar estado general
        if (avgLatency.orElse(0) > 100) {
            sb.append("ALERTA: Latencia elevada detectada\n");
        }
        if (avgPacketLoss.orElse(0) > 1.0) {
            sb.append("ALERTA: Pérdida de paquetes significativa\n");
        }
        if (avgMos.orElse(5) < 3.5) {
            sb.append("ALERTA: Calidad de voz degradada\n");
        }

        sb.append(String.format("\nTotal de muestras: %d%n", metrics.size()));

        return sb.toString();
    }

    /**
     * Construir contexto de dispositivos
     */
    public String buildDeviceContext() {
        List<Dispositivo> authorized = dispositivoRepository.findAll().stream()
                .filter(d -> d.getEstadoAutorizacion() == EstadoAutorizacion.AUTORIZADO).toList();

        List<Dispositivo> unauthorized = dispositivoRepository.findAll().stream()
                .filter(d -> d.getEstadoAutorizacion() == EstadoAutorizacion.NO_AUTORIZADO).toList();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Dispositivos autorizados: %d%n", authorized.size()));
        sb.append(String.format("Dispositivos NO autorizados: %d%n", unauthorized.size()));

        if (!unauthorized.isEmpty()) {
            sb.append("\nDISPOSITIVOS NO AUTORIZADOS DETECTADOS:\n");
            for (Dispositivo dev : unauthorized) {
                sb.append(String.format("  • IP: %s | MAC: %s | Estado: %s%n", dev.getIp(), dev.getMac(),
                        dev.getEstadoAutorizacion()));
            }
            sb.append("\nRECOMENDACIÓN: Investigar estos dispositivos no autorizados inmediatamente.\n");
        }

        return sb.toString();
    }

    /**
     * Obtener el System Prompt desde archivo de configuración
     */
    public String buildSystemPrompt() {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/system-prompt.txt");
            if (resource.exists()) {
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            }
        }
        catch (IOException e) {
            // Log del error - usar fallback
        }

        // Fallback si no existe el archivo
        return getFallbackSystemPrompt();
    }

    /**
     * System Prompt por defecto si no existe archivo externo
     */
    private String getFallbackSystemPrompt() {
        return """
                # ROLE
                Eres "ShieldNet AI", un asistente experto en ciberseguridad y administrador de infraestructura de red.
                Tu propósito es asistir en el monitoreo, diagnóstico y protección de la red corporativa,
                integrando datos de dispositivos y sistemas de telefonía Asterisk.

                # CAPACIDADES TÉCNICAS
                1. Monitoreo de Red: Puedes analizar logs de eventos y métricas de QoS.
                2. Gestión de Dispositivos: Tienes acceso al inventario de dispositivos autorizados y su estado.
                3. Asterisk: Puedes interpretar configuraciones y estados de telefonía IP.
                4. Escaneo: Tienes permiso para ejecutar escaneos ARP y de puertos cuando sea necesario.

                # REGLAS DE RESPUESTA
                1. IDIOMA: Responde siempre en español, manteniendo un tono profesional, técnico y directo.
                2. PRECISIÓN: Prioriza los datos de las herramientas sobre las suposiciones.
                3. SEGURIDAD: Si detectas una IP no autorizada o un puerto crítico abierto, resáltalo como alerta.
                4. CONCISIÓN: No uses introducciones largas. Ve directo al grano con la información técnica.
                5. FORMATO: Utiliza tablas para comparar métricas y bloques de código para configuraciones.
                """;
    }

    /**
     * Descripción del schema de datos disponibles para la IA
     */
    private String getSchemaDescription() {
        return """
                ### eventos
                - id: Identificador único
                - ip: Dirección IP asociada
                - fecha: Fecha del evento
                - hora: Hora del evento
                - descripcion: Descripción detallada del evento
                - usuario_id: Usuario que registró el evento

                Tipos de eventos comunes:
                - ACCESO_NO_AUTORIZADO: Intento de acceso desde IP no registrada
                - DISPOSITIVO_DETECTADO: Nuevo dispositivo en la red
                - ESCANEO_COMPLETADO: Escaneo de red finalizado
                - QOS_DEGRADADO: Degradación en métricas de calidad

                ### qos_metrics
                - id: Identificador único
                - timestamp: Momento de la medición
                - targetIp: IP monitoreada
                - latencyMs: Latencia en milisegundos (normal: < 100ms)
                - jitterMs: Variación en ms (normal: < 10ms)
                - packetLossPercent: Pérdida de paquetes en % (normal: < 1%)
                - mosScore: Calidad de voz (1.0-5.0, siendo 5 excelente)
                - protocolType: VoIP, HTTP, Streaming, Other

                ### dispositivos
                - id: Identificador único
                - ip: Dirección IP
                - mac: Dirección MAC (física)
                - nombre: Nombre/hostname del dispositivo
                - estado: AUTORIZADO, NO_AUTORIZADO, PENDIENTE
                - timestamp: Última vez visto en la red
                """;
    }
}
