package com.example.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/optimizacion/datos")
public class OptimizacionDatosController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<?> obtenerDatos() {

        try {

            // ── X1: Eventos Críticos ──────────────────────────
            Integer dispositivosDesconocidos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dispositivos WHERE estado_autorizacion = 'DESCONOCIDO'",
                Integer.class
            );

            Integer qosMalos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qos_metrics WHERE quality_status IN ('MUY_MALO','MALO','muy_malo','malo','bad','BAD')",
                Integer.class
            );

            int x1 = nullSafe(dispositivosDesconocidos) + nullSafe(qosMalos);

            // ── X2: Eventos Medios ────────────────────────────
            Integer qosMedios = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qos_metrics WHERE quality_status NOT IN ('MUY_MALO','MALO','muy_malo','malo','bad','BAD')",
                Integer.class
            );

            Integer eventosMedios = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eventos WHERE LOWER(descripcion) LIKE '%escan%' OR LOWER(descripcion) LIKE '%puerto%' OR LOWER(descripcion) LIKE '%mac%'",
                Integer.class
            );

            int x2 = nullSafe(qosMedios) + nullSafe(eventosMedios);

            // ── X3: Eventos Bajos ─────────────────────────────
            Integer x3Raw = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eventos WHERE LOWER(descripcion) LIKE '%ping%' OR LOWER(descripcion) LIKE '%usuario%'",
                Integer.class
            );

            int x3 = nullSafe(x3Raw);

            // ── Respuesta ─────────────────────────────────────
            Map<String, Object> response = new HashMap<>();

            response.put("x1", x1);
            response.put("x2", x2);
            response.put("x3", x3);

            response.put("dispositivosDesconocidos", nullSafe(dispositivosDesconocidos));
            response.put("qosMalos", nullSafe(qosMalos));
            response.put("qosMedios", nullSafe(qosMedios));
            response.put("eventosMedios", nullSafe(eventosMedios));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    private int nullSafe(Integer value) {
        return value != null ? value : 0;
    }
}