package com.example.security.modelo.chat;

/**
 * Solicitud de herramienta para ejecutar escaneo de red.
 * Utilizada por Spring AI Function Calling.
 */
public class NetworkScanRequest {
    
    private String type; // "arp" o "port"
    private String subnet; // Ej: "192.168.1.0/24"
    
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
