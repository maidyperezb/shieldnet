package com.example.security.modelo;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ip;

    // Campo MAC nuevo
    @Column(name = "mac", length = 17)
    private String mac;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultima_conexion")
    private Date ultimaConexion;

    // Campo para almacenar el estado de autorización como un String en la BD
    @Column(name = "estado_autorizacion", length = 20)
    private String estadoAutorizacionStr;

    // Propiedad transitoria para manejar el enum
    @Transient
    private EstadoAutorizacion estadoAutorizacion;

    // Constructor vacío
    public Dispositivo() {
    }

    public Dispositivo(String ip, Date ultimaConexion) {
        this.ip = ip;
        this.ultimaConexion = ultimaConexion;
        this.calcularEstadoAutorizacion();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac != null ? mac.toUpperCase().trim() : null;
    }

    public Date getUltimaConexion() {
        return ultimaConexion;
    }

    public void setUltimaConexion(Date ultimaConexion) {
        this.ultimaConexion = ultimaConexion;
    }

    public EstadoAutorizacion getEstadoAutorizacion() {
        if (estadoAutorizacion == null && estadoAutorizacionStr != null) {
            try {
                estadoAutorizacion = EstadoAutorizacion.valueOf(estadoAutorizacionStr);
            } catch (IllegalArgumentException e) {
                estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
            }
        }
        return estadoAutorizacion != null ? estadoAutorizacion : EstadoAutorizacion.DESCONOCIDO;
    }

    public void setEstadoAutorizacion(EstadoAutorizacion estadoAutorizacion) {
        this.estadoAutorizacion = estadoAutorizacion;
        this.estadoAutorizacionStr = estadoAutorizacion != null ? estadoAutorizacion.name() : null;
    }

    // Getters y setters para el campo de base de datos
    public String getEstadoAutorizacionStr() {
        return estadoAutorizacionStr;
    }

    public void setEstadoAutorizacionStr(String estadoAutorizacionStr) {
        this.estadoAutorizacionStr = estadoAutorizacionStr;
        if (estadoAutorizacionStr != null) {
            try {
                this.estadoAutorizacion = EstadoAutorizacion.valueOf(estadoAutorizacionStr);
            } catch (IllegalArgumentException e) {
                this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
            }
        }
    }

    /**
     * Ya no clasifica por rango de IP.
     * El estado lo determina ArpScannerService según la MAC.
     * Solo establece DESCONOCIDO si no tiene estado aún.
     */
    private void calcularEstadoAutorizacion() {
        if (this.estadoAutorizacion == null && this.estadoAutorizacionStr == null) {
            this.estadoAutorizacion = EstadoAutorizacion.DESCONOCIDO;
            this.estadoAutorizacionStr = EstadoAutorizacion.DESCONOCIDO.name();
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        calcularEstadoAutorizacion();
    }
}