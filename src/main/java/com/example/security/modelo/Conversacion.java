package com.example.security.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad para almacenar conversaciones del chat AI.
 * Mantiene historial completo incluyendo prompts, respuestas y tool calls.
 */
@Entity
@Table(name = "conversaciones", indexes = {
    @Index(name = "idx_usuario_created", columnList = "usuario_id, created_at DESC")
})
public class Conversacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String prompt;
    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String respuesta;
    
    @Column(columnDefinition = "LONGTEXT")
    private String toolCalls; // JSON con invocaciones intermedias de herramientas
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs; // Tiempo de respuesta de la API
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // Soft delete: null = activo, no null = eliminado
    
    // Constructores
    public Conversacion() {}
    
    public Conversacion(Usuario usuario, String prompt, String respuesta) {
        this.usuario = usuario;
        this.prompt = prompt;
        this.respuesta = respuesta;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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
    
    public String getToolCalls() {
        return toolCalls;
    }
    
    public void setToolCalls(String toolCalls) {
        this.toolCalls = toolCalls;
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
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
