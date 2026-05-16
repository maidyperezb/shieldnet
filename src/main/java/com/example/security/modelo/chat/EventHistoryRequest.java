package com.example.security.modelo.chat;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Solicitud de herramienta para consultar historial de eventos. Utilizada por
 * Spring AI Function Calling.
 */
@JsonClassDescription("Consulta el historial de eventos de seguridad de la red en un rango de fechas especificado")
public class EventHistoryRequest {

    @JsonPropertyDescription("Fecha de inicio del rango (default: 7 días atrás)")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startDate;

    @JsonPropertyDescription("Fecha de fin del rango (default: hoy)")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endDate;

    @JsonPropertyDescription("Número máximo de resultados a retornar (default: 100)")
    private Integer maxResults;

    public EventHistoryRequest() {
    }

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
