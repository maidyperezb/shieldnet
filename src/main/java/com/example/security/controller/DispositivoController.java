package com.example.security.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.security.dto.DispositivoDTO;
import com.example.security.modelo.Dispositivo;
import com.example.security.servicios.ArpScannerService;
import com.example.security.servicios.DispositivoService;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    @Autowired
    private DispositivoService dispositivoService;

    @Autowired
    private ArpScannerService arpScannerService;

    /**
     * Obtiene todos los dispositivos
     */
    @GetMapping
    public ResponseEntity<List<Dispositivo>> getAllDispositivos() {
        List<Dispositivo> dispositivos = dispositivoService.findAll();
        return new ResponseEntity<>(dispositivos, HttpStatus.OK);
    }

    /**
     * Registra un nuevo dispositivo o actualiza uno existente
     */
    @PostMapping
    public ResponseEntity<?> registrarDispositivo(@RequestBody DispositivoDTO dispositivoDTO) {
        try {
            Date timestamp = new Date();
            Dispositivo dispositivo = new Dispositivo();
            dispositivo.setIp(dispositivoDTO.getIp());
            dispositivo.setUltimaConexion(timestamp);
            Dispositivo savedDispositivo = dispositivoService.saveDispositivo(dispositivo);
            return new ResponseEntity<>(savedDispositivo, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                "Error al procesar la solicitud: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Buscar dispositivo por IP
     */
    @GetMapping("/ip/{ip}")
    public ResponseEntity<Dispositivo> getDispositivoByIp(@PathVariable String ip) {
        Dispositivo dispositivo = dispositivoService.findByIp(ip);
        if (dispositivo != null) {
            return new ResponseEntity<>(dispositivo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Iniciar el ARP Scanner manualmente
     */
    @PostMapping("/scanner/start")
    public ResponseEntity<String> startScanner() {
        arpScannerService.start();
        return ResponseEntity.ok("ARP Scanner iniciado");
    }

    /**
     * Detener el ARP Scanner manualmente
     */
    @PostMapping("/scanner/stop")
    public ResponseEntity<String> stopScanner() {
        arpScannerService.stop();
        return ResponseEntity.ok("ARP Scanner detenido");
    }

    /**
     * Estado del scanner
     */
    @GetMapping("/scanner/status")
    public ResponseEntity<String> scannerStatus() {
        return ResponseEntity.ok(arpScannerService.isRunning() ? "running" : "stopped");
    }

    /**
     * SSE para recibir nuevos dispositivos en tiempo real
     */
    @GetMapping(value = "/scanner/events", produces = "text/event-stream")
    public SseEmitter streamDevices() {
        return arpScannerService.subscribe();
    }

    /**
     * Dispara un escaneo ARP manual de toda la red
     */
    @PostMapping("/scanner/scan")
    public ResponseEntity<String> scanNow() {
        arpScannerService.scanNetwork();
        return ResponseEntity.ok("Escaneo iniciado");
}
}