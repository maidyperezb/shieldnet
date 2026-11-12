package com.example.security.repositorio;

import com.example.security.modelo.MacAutorizada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MacAutorizadaRepository extends JpaRepository<MacAutorizada, Long> {
    /**
     * Devuelve el registro de la MAC sea activo o inactivo (la distinción la
     * hace el servicio). No existe una consulta 'existsByMac' a propósito: una
     * consulta que ignore el estado haría que una MAC inactiva pareciera
     * existente/autorizada.
     */
    MacAutorizada findByMac(String mac);
}