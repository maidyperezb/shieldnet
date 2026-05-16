package com.example.security.dto;

import com.example.security.modelo.Conversacion;
import java.time.LocalDateTime;

/**
 * DTO para transferir datos de Conversacion en respuestas REST.
 * Omite campos sensibles y optimiza serialización.
 */
public class ConversacionDTO {
    
    private Long id;
    private String prompt;
    private String respuesta;
    private LocalDateTime createdAt;
    private Integer tokensUsed;
    private Long responseTimeMs;
    
    // Constructores
    public ConversacionDTO() {}
    
    public ConversacionDTO(Long id, String prompt, String respuesta, LocalDateTime createdAt) {
        this.id = id;
        this.prompt = prompt;
        this.respuesta = respuesta;
        this.createdAt = createdAt;
    }
    
    /**
     * Convertir entidad a DTO
     */
    public static ConversacionDTO from(Conversacion conversacion) {
        ConversacionDTO dto = new ConversacionDTO();
        dto.setId(conversacion.getId());
        dto.setPrompt(conversacion.getPrompt());
        dto.setRespuesta(conversacion.getRespuesta());
        dto.setCreatedAt(conversacion.getCreatedAt());
        dto.setTokensUsed(conversacion.getTokensUsed());
        dto.setResponseTimeMs(conversacion.getResponseTimeMs());
        return dto;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public String getRespuesta() {
        return respuesta;
    }
    
    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Integer getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
    
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }
    
    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}
