package com.example.security.modelo.chat;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Solicitud de herramienta para consultar métricas QoS. Utilizada por Spring AI
 * Function Calling.
 */
@JsonClassDescription("Consulta las métricas de Calidad de Servicio (QoS) de la red como latencia, jitter, pérdida de paquetes y puntuación MOS")
public class QoSMetricsRequest {

    @JsonPropertyDescription("Número de días anteriores para consultar (default: 7)")
    private Integer days;

    @JsonPropertyDescription("Tipo de protocolo a filtrar: VoIP, HTTP, Streaming u Other (opcional)")
    private String protocolType;

    public QoSMetricsRequest() {
    }

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
