package com.example.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/api/optimizacion")
public class OptimizacionController {

    @GetMapping
    public ResponseEntity<?> ejecutarModelo() {

        try {

            ProcessBuilder pb = new ProcessBuilder(
                 "C:\\Users\\MAIDY Y MICHELL\\AppData\\Local\\Programs\\Python\\Python313\\python.exe",
            "C:\\Users\\MAIDY Y MICHELL\\Downloads\\Software2 - ShieldNet - Thay\\Software2 - ShieldNet, funciona\\Software - ShieldNet\\security-demo (1)-20250423T174720Z-001\\security-demo (1)\\security-demo\\Modelo Matematico\\shieldnet_optimizacion.py"
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            process.waitFor();

            String resultado = output.toString().trim();

            if (resultado.isEmpty()) {
                return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "success", false,
                        "error", "El script Python no devolvió ningún resultado"
                    ));
            }

            // Envuelve el JSON de Python en { "resultado": "..." }
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