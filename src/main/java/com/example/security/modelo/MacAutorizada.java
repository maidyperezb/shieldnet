package com.example.security.modelo;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "macs_autorizadas")
public class MacAutorizada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mac", nullable = false, unique = true, length = 17)
    private String mac;

    @Column(name = "descripcion")
    private String descripcion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_registro")
    private Date fechaRegistro;

    public MacAutorizada() {}

    public MacAutorizada(String mac, String descripcion) {
        this.mac = mac.toUpperCase().trim();
        this.descripcion = descripcion;
        this.fechaRegistro = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac.toUpperCase().trim(); }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}