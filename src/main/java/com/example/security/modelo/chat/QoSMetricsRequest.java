package com.example.security.modelo.chat;

/**
 * Solicitud de herramienta para consultar métricas QoS.
 * Utilizada por Spring AI Function Calling.
 */
public class QoSMetricsRequest {
    
    private Integer days;
    private String protocolType; // VoIP, HTTP, Streaming, Other (opcional)
    
    public QoSMetricsRequest() {}
    
    public QoSMetricsRequest(Integer days) {
        this.days = days;
    }
    
    public QoSMetricsRequest(Integer days, String protocolType) {
        this.days = days;
        this.protocolType = protocolType;
    }
    
    // Getters y Setters
    public Integer getDays() {
        return days;
    }
    
    public void setDays(Integer days) {
        this.days = days;
    }
    
    public String getProtocolType() {
        return protocolType;
    }
    
    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }
}
