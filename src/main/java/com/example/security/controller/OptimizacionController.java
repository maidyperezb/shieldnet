package com.example.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/optimizacion")
public class OptimizacionController {

    @GetMapping
    public ResponseEntity<?> ejecutarModelo() {

        try {

            // Llamada HTTP al servicio de optimización.
            // Usa variables de entorno para permitir ejecución local o en Docker.
            String host = System.getenv().getOrDefault("OPTIMIZACION_HOST", System.getenv().getOrDefault("OPTIMIZACION_HOSTNAME", "localhost"));
            String port = System.getenv().getOrDefault("OPTIMIZACION_PORT", System.getenv().getOrDefault("OPTIMIZACION_PORT_NUMBER", "5060"));
            String url = String.format("http://%s:%s/optimize", host, port);
            RestTemplate rt = new RestTemplate();
            String resultado = rt.getForObject(url, String.class);

            if (resultado == null || resultado.trim().isEmpty()) {
                return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "success", false,
                        "error", "El servicio de optimización no devolvió resultado"
                    ));
            }

            return ResponseEntity.ok(Map.of("resultado", resultado));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
                ));
        }
    }
}