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

            // Llamada HTTP al servicio de optimización (contenedor 'optimizacion')
            RestTemplate rt = new RestTemplate();
            String url = "http://optimizacion:5060/optimize";
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