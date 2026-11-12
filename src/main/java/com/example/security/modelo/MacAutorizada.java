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

    /**
     * Estado del registro. DEFAULT TRUE en el DDL para que las MACs existentes
     * queden activas tras la actualización del esquema (ddl-auto=update no
     * aplica el valor inicial de Java a las filas preexistentes).
     */
    @Column(name = "activo", nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private boolean activo = true;

    public MacAutorizada() {}

    public MacAutorizada(String mac, String descripcion) {
        this.mac = normalizar(mac);
        this.descripcion = descripcion;
        this.fechaRegistro = new Date();
    }

    /**
     * Forma canónica de una MAC en todo el sistema: mayúsculas, sin espacios y
     * con separadores ':' (el escáner ARP almacena {@code AA:BB:CC:DD:EE:FF} y
     * la validación del formulario también admite guiones). Usar este método en
     * las consultas evita que una MAC con guiones no coincida con su registro.
     */
    public static String normalizar(String mac) {
        return mac == null ? null : mac.toUpperCase().trim().replace('-', ':');
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = normalizar(mac); }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}