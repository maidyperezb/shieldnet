package com.example.security.servicios;

import com.example.security.modelo.Conversacion;
import com.example.security.modelo.Usuario;
import com.example.security.repositorio.ConversacionRepository;
import com.example.security.repositorio.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
 * Servicio principal de Chat que orquesta el flujo completo. Integra
 * ContextBuilder, ChatAIService y persistencia de conversaciones.
 */
@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ContextBuilder contextBuilder;

    @Autowired
    private ConversacionRepository conversacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Procesar un prompt del usuario y retornar Flux<String> para streaming SSE.
     * Construye contexto, invoca AI, maneja tools, y persiste resultado.
     */
    public Flux<String> procesarPrompt(String query, Long usuarioId) {
        return Flux.defer(() -> {
            try {
                // Obtener usuario
                Usuario usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

                long startTime = System.currentTimeMillis();

                // 1. Determinar límite de contexto dinámico según la petición del usuario
                int defaultMax = contextBuilder.getMaxContextChars();
                int allowed;
                String cleanedQuery = query == null ? "" : query;

                if (cleanedQuery.contains("[context:short]")) {
                    allowed = Math.min(defaultMax, 2000);
                    cleanedQuery = cleanedQuery.replace("[context:short]", "").trim();
                }
                else if (cleanedQuery.contains("[context:medium]")) {
                    allowed = Math.min(defaultMax, 8000);
                    cleanedQuery = cleanedQuery.replace("[context:medium]", "").trim();
                }
                else if (cleanedQuery.contains("[context:full]")) {
                    allowed = defaultMax;
                    cleanedQuery = cleanedQuery.replace("[context:full]", "").trim();
                }
                else {
                    // Heurística: reducir el contexto en función de la longitud de la pregunta
                    allowed = Math.max(2000, defaultMax - Math.min(10000, cleanedQuery.length() * 12));
                }

                // Construir contexto con el límite calculado
                String context = contextBuilder.buildFullContext(usuario, allowed);
                String systemPrompt = contextBuilder.buildSystemPrompt();

                int payloadLength = (systemPrompt == null ? 0 : systemPrompt.length()) + (context == null ? 0 : context.length()) + cleanedQuery.length();
                logger.debug("Context length used: {} chars (allowed {})", context.length(), allowed);
                logger.debug("Payload length (system+context+query): {} chars", payloadLength);

                // Si el payload total excede el límite permitido, intentar reducir el contexto más agresivamente
                if (payloadLength > allowed) {
                    int reducedAllowed = Math.max(1000, allowed / 3);
                    logger.info("Payload {} > allowed {} — reduciendo contexto a {} chars", payloadLength, allowed, reducedAllowed);
                    context = contextBuilder.buildFullContext(usuario, reducedAllowed);
                    payloadLength = (systemPrompt == null ? 0 : systemPrompt.length()) + (context == null ? 0 : context.length()) + cleanedQuery.length();
                    logger.debug("After reduction payload length: {} chars", payloadLength);
                }

                // Si sigue excediendo, usar resumen corto como último recurso
                if (payloadLength > allowed) {
                    logger.info("Payload sigue excediendo allowed; usando resumen corto");
                    context = contextBuilder.buildShortSummary(usuario);
                    payloadLength = (systemPrompt == null ? 0 : systemPrompt.length()) + (context == null ? 0 : context.length()) + cleanedQuery.length();
                    logger.debug("Payload length after short summary: {} chars", payloadLength);
                }

                logger.info("Iniciando streaming real para usuario: {}", usuarioId);

                // 2. Invocar ChatClient (streaming real)
                StringBuilder fullResponse = new StringBuilder();

                return chatClient.prompt()
                    .system(systemPrompt)
                    .user(context + "\n\nPregunta: " + cleanedQuery)
                        .stream()
                        .content()
                        .doOnNext(fullResponse::append)
                        .doOnComplete(() -> {
                            // 3. Guardar conversación en BD al finalizar
                            long responseTime = System.currentTimeMillis() - startTime;
                            saveConversation(usuario, query, fullResponse.toString(), responseTime);
                            logger.info("Streaming completado y conversación guardada para usuario: {}", usuarioId);
                        })
                        .doOnError(e -> logger.error("Error durante el streaming de ChatClient", e));

            } catch (Exception e) {
                logger.error("Error inicializando procesarPrompt", e);
                return Flux.error(e);
            }
        });
    }

    /**
     * Obtener historial de conversaciones del usuario
     */
    public java.util.List<Conversacion> obtenerHistorial(Long usuarioId) {
        return conversacionRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);
    }

    /**
     * Obtener últimas N conversaciones del usuario
     */
    public java.util.List<Conversacion> obtenerUltimasConversaciones(Long usuarioId, int limit) {
        return conversacionRepository.findRecentConversations(usuarioId, limit);
    }

    /**
     * Limpiar historial completo del usuario (soft delete)
     */
    public void limpiarHistorial(Long usuarioId) {
        conversacionRepository.softDeleteByUsuarioId(usuarioId);
        logger.info("Historial de conversaciones marcado como eliminado para usuario: {}", usuarioId);
    }

    /**
     * Limpiar conversaciones antiguas (más de N días) - soft delete
     */
    public long limpiarConversacionesAntiguas(int diasRetention) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(diasRetention);
        java.util.List<Conversacion> toDelete = conversacionRepository.findByCreatedAtBefore(cutoffDate);
        toDelete.forEach(Conversacion::softDelete);
        conversacionRepository.saveAll(toDelete);
        logger.info("Se marcaron {} conversaciones antiguas (> {} días) como eliminadas", toDelete.size(),
                diasRetention);
        return toDelete.size();
    }

    /**
     * Guardar conversación en BD
     */
    private void saveConversation(Usuario usuario, String prompt, String respuesta, long responseTimeMs) {
        try {
            Conversacion conv = new Conversacion();
            conv.setUsuario(usuario);
            conv.setPrompt(prompt);
            conv.setRespuesta(respuesta);
            conv.setResponseTimeMs(responseTimeMs);
            conv.setCreatedAt(LocalDateTime.now());

            conversacionRepository.save(conv);
            logger.debug("Conversación guardada: id={}, usuario={}, tiempo={}ms", conv.getId(), usuario.getId(),
                    responseTimeMs);
        }
        catch (Exception e) {
            logger.error("Error guardando conversación", e);
            // No lanzar excepción - no afecta al usuario
        }
    }

    /**
     * Dividir texto en chunks de aproximadamente N caracteres
     */
    private String[] splitIntoChunks(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        java.util.List<String> chunks = new java.util.ArrayList<>();
        int numChunks = (int) Math.ceil((double) text.length() / chunkSize);

        for (int i = 0; i < numChunks; i++) {
            int start = i * chunkSize;
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
        }

        return chunks.toArray(new String[0]);
    }
}
