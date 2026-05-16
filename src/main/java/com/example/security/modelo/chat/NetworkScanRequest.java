package com.example.security.modelo.chat;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Solicitud de herramienta para ejecutar escaneo de red.
 * Utilizada por Spring AI Function Calling.
 */
@JsonClassDescription("Ejecuta un escaneo de red para descubrir dispositivos y sus direcciones IP/MAC")
public class NetworkScanRequest {
    
    @JsonPropertyDescription("Tipo de escaneo: 'arp' para descubrir dispositivos o 'port' para escanear puertos")
    private String type;
    
    @JsonPropertyDescription("Subred a escanear en formato CIDR (Ej: 192.168.1.0/24)")
    private String subnet;
    
    public NetworkScanRequest() {}
    
    public NetworkScanRequest(String type, String subnet) {
        this.type = type;
        this.subnet = subnet;
    }
    
    // Getters y Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSubnet() {
        return subnet;
    }
    
    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }
}
