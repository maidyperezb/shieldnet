package com.example.security.modelo.chat;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Solicitud de herramienta para generar reporte de monitoreo. Utilizada por
 * Spring AI Function Calling.
 */
@JsonClassDescription("Genera un reporte completo de monitoreo incluyendo eventos, métricas QoS y estado de dispositivos")
public class ReportRequest {

    @JsonPropertyDescription("Número de días para incluir en el reporte (default: 7)")
    private Integer days;

    public ReportRequest() {
    }

    public ReportRequest(Integer days) {
        this.days = days;
    }

    // Getters y Setters
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}
