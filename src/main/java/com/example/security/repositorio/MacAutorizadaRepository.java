package com.example.security.repositorio;

import com.example.security.modelo.MacAutorizada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MacAutorizadaRepository extends JpaRepository<MacAutorizada, Long> {
    MacAutorizada findByMac(String mac);
    boolean existsByMac(String mac);
}