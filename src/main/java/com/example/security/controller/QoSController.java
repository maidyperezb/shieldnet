package com.example.security.controller;

import com.example.security.dto.QoSData;
import com.example.security.modelo.QoSMetrics;
import com.example.security.servicios.EventoService;
import com.example.security.servicios.QoSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/network/qos")
@CrossOrigin(origins = "*")
public class QoSController {

    @Autowired
    private QoSService qosService;

    @Autowired
private EventoService eventoService;

    /**
     * POST /api/network/qos/measure
     * Medición manual de QoS
     */
    @PostMapping("/measure")
public ResponseEntity<QoSData> measureQoS(@RequestBody Map<String, Object> request) {
    System.out.println("=== QoSController.measureQoS LLAMADO ===");
    try {
        String ip = (String) request.get("ip");

        int packets = 20;
        if (request.containsKey("packets")) {
            Object packetsObj = request.get("packets");
            if (packetsObj instanceof Integer) {
                packets = (Integer) packetsObj;
            } else if (packetsObj instanceof String) {
                packets = Integer.parseInt((String) packetsObj);
            }
        }

        if (ip == null || ip.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        QoSData result = qosService.measureQoS(ip, packets);
        result.setTargetIp(ip);
        result.setDuringActiveCall(false);

        System.out.println("=== ANTES DE REGISTRAR EVENTO QoS ===");
        try {
            eventoService.registrarEvento(
                "Medición QoS a " + ip +
                " — Latencia: " + String.format("%.1f", result.getLatencyMs()) + "ms" +
                ", MOS: " + String.format("%.2f", result.getMosScore()) +
                ", Estado: " + result.getQualityStatus()
            );
            System.out.println("=== EVENTO QoS REGISTRADO OK ===");
        } catch (Exception ex) {
            System.err.println("=== ERROR REGISTRANDO EVENTO QoS: " + ex.getMessage());
            ex.printStackTrace();
        }

        return ResponseEntity.ok(result);

    } catch (Exception e) {
        System.err.println("=== ERROR GENERAL en measureQoS: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError().build();
    }
}

    /**
     * GET /api/network/qos/latest
     * Obtiene las últimas 50 métricas
     */
    
    @GetMapping("/latest")
    public ResponseEntity<List<QoSMetrics>> getLatestMetrics(
            @RequestParam(defaultValue = "0") int hours) {
        List<QoSMetrics> metrics;
        if (hours > 0) {
            metrics = qosService.getRecentMetrics(hours);
        } else {
            metrics = qosService.getAllMetrics(); 
        }
        return ResponseEntity.ok(metrics);
    }

    
    @GetMapping("/mos-average")
    public ResponseEntity<Map<String, Double>> getAverageMos() {
        Double avgMos = qosService.getAverageMosScore();
        return ResponseEntity.ok(Map.of("averageMos", avgMos != null ? avgMos : 0.0));
    }

    /**
     * ⭐ NUEVO ENDPOINT: Comparación de métricas
     * GET /api/network/qos/comparison
     */
    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getComparison() {
        Map<String, Object> comparison = qosService.getComparativeMetrics();
        return ResponseEntity.ok(comparison);
    }
}