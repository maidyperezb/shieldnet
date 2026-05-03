package com.example.security.servicios;

import com.example.security.modelo.MacAutorizada;
import com.example.security.repositorio.MacAutorizadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MacAutorizadaService {

    @Autowired
    private MacAutorizadaRepository macAutorizadaRepository;

    public List<MacAutorizada> findAll() {
        return macAutorizadaRepository.findAll();
    }

    public boolean esMacAutorizada(String mac) {
        if (mac == null) return false;
        return macAutorizadaRepository.existsByMac(mac.toUpperCase().trim());
    }

    public MacAutorizada agregarMac(String mac, String descripcion) {
        String macNormalizada = mac.toUpperCase().trim();
        if (macAutorizadaRepository.existsByMac(macNormalizada)) {
            return macAutorizadaRepository.findByMac(macNormalizada);
        }
        return macAutorizadaRepository.save(new MacAutorizada(macNormalizada, descripcion));
    }

    public void eliminarMac(Long id) {
        macAutorizadaRepository.deleteById(id);
    }
}