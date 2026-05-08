package com.example.security.modelo.chat;

/**
 * Solicitud de herramienta para generar reporte de monitoreo.
 * Utilizada por Spring AI Function Calling.
 */
public class ReportRequest {
    
    private Integer days;
    
    public ReportRequest() {}
    
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
