package com.example.security.controller;

import com.example.security.modelo.MacAutorizada;
import com.example.security.modelo.Dispositivo;
import com.example.security.repositorio.DispositivoRepository;
import com.example.security.modelo.EstadoAutorizacion;
import com.example.security.servicios.MacAutorizadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.security.servicios.EventoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/macs")
public class MacAutorizadaController {

    @Autowired
    private MacAutorizadaService macAutorizadaService;

    @Autowired
    private DispositivoRepository dispositivoRepository;

    @Autowired
    private EventoService eventoService;

    @GetMapping
    public ResponseEntity<List<MacAutorizada>> getAll() {
        return ResponseEntity.ok(macAutorizadaService.findAll());
    }

    @PostMapping
    public ResponseEntity<?> agregar(@RequestBody Map<String, String> body) {
        String mac = body.get("mac");
        String descripcion = body.getOrDefault("descripcion", "");

        if (mac == null || mac.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("MAC requerida");
        }

        if (!mac.matches("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")) {
            return ResponseEntity.badRequest().body("Formato de MAC inválido. Ejemplo: AA:BB:CC:DD:EE:FF");
        }

        MacAutorizada saved = macAutorizadaService.agregarMac(mac, descripcion);

        List<Dispositivo> dispositivos = dispositivoRepository.findByMac(mac.toUpperCase().trim());
        for (Dispositivo d : dispositivos) {
            d.setEstadoAutorizacion(EstadoAutorizacion.AUTORIZADO);
            dispositivoRepository.save(d);
        }

        eventoService.registrarEvento(
            "MAC autorizada agregada: " + mac +
            (descripcion.isEmpty() ? "" : " — " + descripcion)
        );

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
public ResponseEntity<?> eliminar(@PathVariable Long id) {
    macAutorizadaService.eliminarMac(id);
    eventoService.registrarEvento("MAC autorizada eliminada (ID: " + id + ")");
    return ResponseEntity.ok("MAC eliminada");
}
}