package com.example.security.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.security.modelo.Dispositivo;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {
    
    Dispositivo findByIp(String ip);
    List<Dispositivo> findByMac(String mac);
}