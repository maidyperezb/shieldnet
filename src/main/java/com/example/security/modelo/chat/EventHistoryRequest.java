package com.example.security.modelo.chat;

import java.time.LocalDateTime;

/**
 * Solicitud de herramienta para consultar historial de eventos.
 * Utilizada por Spring AI Function Calling.
 */
public class EventHistoryRequest {
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxResults;
    
    public EventHistoryRequest() {}
    
    public EventHistoryRequest(LocalDateTime startDate, LocalDateTime endDate, Integer maxResults) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxResults = maxResults;
    }
    
    // Getters y Setters
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public Integer getMaxResults() {
        return maxResults;
    }
    
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }
}
