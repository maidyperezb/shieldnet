package com.example.security.controller;

import com.example.security.modelo.MacAutorizada;
import com.example.security.servicios.MacAutorizadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.security.servicios.EventoService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/macs")
public class MacAutorizadaController {

    @Autowired
    private MacAutorizadaService macAutorizadaService;

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

        // Si el registro existe inactivo se reactiva (mismo id y fechaRegistro);
        // la sincronización de los dispositivos asociados la realiza el servicio
        // en la misma transacción.
        MacAutorizada saved = macAutorizadaService.agregarMac(mac, descripcion);

        eventoService.registrarEvento(
            "MAC autorizada agregada: " + saved.getMac() +
            (descripcion.isEmpty() ? "" : " — " + descripcion)
        );

        return ResponseEntity.ok(saved);
    }

    /**
     * Desactiva una MAC autorizada sin eliminar su registro. La MAC deja de
     * autorizar dispositivos (que pasan a NO_AUTORIZADO) y puede reactivarse.
     */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try {
            MacAutorizada mac = macAutorizadaService.desactivarMac(id);
            eventoService.registrarEvento("MAC autorizada desactivada (ID: " + id + ")");
            return ResponseEntity.ok(mac);
        } catch (IllegalStateException e) {
            // Transición inválida: ya estaba desactivada
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Reactiva una MAC autorizada conservando su identificador y fecha de
     * registro. Los dispositivos asociados vuelven a AUTORIZADO.
     */
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivar(@PathVariable Long id) {
        try {
            MacAutorizada mac = macAutorizadaService.reactivarMac(id);
            eventoService.registrarEvento("MAC autorizada reactivada (ID: " + id + ")");
            return ResponseEntity.ok(mac);
        } catch (IllegalStateException e) {
            // Transición inválida: ya estaba activa
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
