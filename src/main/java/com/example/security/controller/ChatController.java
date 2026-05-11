package com.example.security.controller;

import com.example.security.dto.ConversacionDTO;
import com.example.security.modelo.Conversacion;
import com.example.security.repositorio.ConversacionRepository;
import com.example.security.repositorio.UsuarioRepository;
import com.example.security.servicios.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Controlador REST para la API de Chat. Expone 3 endpoints: POST
 * /api/chat/message (SSE streaming), GET /history, DELETE /history
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE
})
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConversacionRepository conversacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * POST /api/chat/message Procesa un mensaje del usuario y retorna streaming
     * SSE.
     * 
     * @param query          La pregunta/prompt del usuario
     * @param sessionId      ID de sesión (opcional)
     * @param authentication Usuario autenticado
     * @return SseEmitter para streaming
     */
    @PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleMessage(@RequestParam
    String query, @RequestParam(required = false)
    String sessionId, Authentication authentication) {

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo 'query' no puede estar vacío");
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        Long usuarioId = getUserIdFromAuthentication(authentication);

        logger.info("Chat request from user: {}, query length: {}", usuarioId, query.length());

        // Crear emitter con timeout de 5 minutos
        SseEmitter emitter = new SseEmitter(300_000L);

        // Procesar en hilo aparte para no bloquear
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Obtener Flux de respuesta del servicio
                Flux<String> responseFlux = chatService.procesarPrompt(query, usuarioId);

                // Suscribirse y emitir eventos SSE
                responseFlux.subscribe(chunk -> {
                    try {
                        // Emitir cada chunk como evento SSE
                        emitter.send(SseEmitter.event().name("message").id(UUID.randomUUID().toString()).data(chunk)
                                .build());
                    }
                    catch (IOException e) {
                        logger.error("Error enviando chunk SSE", e);
                    }
                }, error -> {
                    logger.error("Error en flux de respuesta", error);
                    try {
                        emitter.send(SseEmitter.event().name("error")
                                .data("Error procesando solicitud: " + error.getMessage()).build());
                        emitter.complete();
                    }
                    catch (IOException e) {
                        logger.error("Error enviando evento de error", e);
                    }
                }, () -> {
                    try {
                        // Enviar evento de finalización
                        emitter.send(SseEmitter.event().name("complete").data("Respuesta completada").build());
                        emitter.complete();
                        logger.info("Streaming completado para usuario: {}", usuarioId);
                    }
                    catch (IOException e) {
                        logger.error("Error completando emitter", e);
                    }
                });

            }
            catch (Exception e) {
                logger.error("Error en handleMessage", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("Error interno: " + e.getMessage()).build());
                }
                catch (IOException ex) {
                    logger.error("Error enviando error final", ex);
                }
            }
        });

        return emitter;
    }

    /**
     * GET /api/chat/history Obtiene el historial de conversaciones del usuario
     * autenticado.
     * 
     * @param authentication Usuario autenticado
     * @param limit          Número máximo de conversaciones a retornar (default:
     *                           20)
     * @return Lista de ConversacionDTO
     */
    @GetMapping("/history")
    public ResponseEntity<List<ConversacionDTO>> getHistory(Authentication authentication,
            @RequestParam(defaultValue = "20")
            int limit) {
        Long usuarioId = getUserIdFromAuthentication(authentication);

        List<Conversacion> conversaciones = chatService.obtenerUltimasConversaciones(usuarioId, limit);

        List<ConversacionDTO> dtos = conversaciones.stream().map(ConversacionDTO::from).collect(Collectors.toList());

        logger.info("Historial obtenido para usuario: {}, registros: {}", usuarioId, dtos.size());

        return ResponseEntity.ok(dtos);
    }

    /**
     * DELETE /api/chat/history Limpia el historial de conversaciones del usuario
     * autenticado.
     * 
     * @param authentication Usuario autenticado
     * @return Respuesta vacía (204 No Content)
     */
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(Authentication authentication) {
        Long usuarioId = getUserIdFromAuthentication(authentication);

        long countBefore = conversacionRepository.countByUsuarioId(usuarioId);
        chatService.limpiarHistorial(usuarioId);

        logger.info("Historial eliminado para usuario: {} ({} registros)", usuarioId, countBefore);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/chat/count Obtiene el número de conversaciones del usuario. Endpoint
     * informativo para auditoría y estadísticas.
     * 
     * @param authentication Usuario autenticado
     * @return Objeto con el contador
     */
    @GetMapping("/count")
    public ResponseEntity<java.util.Map<String, Long>> getConversationCount(Authentication authentication) {
        Long usuarioId = getUserIdFromAuthentication(authentication);
        long count = conversacionRepository.countByUsuarioId(usuarioId);

        return ResponseEntity.ok(java.util.Map.of("count", count));
    }

    /**
     * Helper: Extraer ID de usuario desde Authentication
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        String username = authentication.getName();
        return usuarioRepository.findByUsername(username).map(user -> user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }
}
