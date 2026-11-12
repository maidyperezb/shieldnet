package com.example.security.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/optimizacion")
public class OptimizacionController {

    private static final Logger log = LoggerFactory.getLogger(OptimizacionController.class);

    /** Ruta del script relativa a cualquier directorio del proyecto. */
    private static final String SCRIPT_RELATIVE_PATH = "Modelo Matematico/shieldnet_optimizacion.py";

    /** Máximo de caracteres de salida Python que se devuelven en un mensaje de error. */
    private static final int MAX_DETALLE = 500;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Intérprete Python. Vacío = autodetectar el venv junto al script. */
    @Value("${optimizacion.python:}")
    private String pythonCommand;

    /** Ruta del script. Vacío = autodetectar buscando hacia arriba desde el directorio de trabajo. */
    @Value("${optimizacion.script:}")
    private String scriptPath;

    // Credenciales de BD: se reenvían al proceso Python para que no dependan
    // de cómo se lance la aplicación (el .env solo lo lee VS Code vía envFile).
    @Value("${MYSQL_HOST:localhost}")
    private String mysqlHost;

    @Value("${MYSQL_PORT:3306}")
    private String mysqlPort;

    @Value("${MYSQL_DATABASE:shieldnet}")
    private String mysqlDatabase;

    @Value("${MYSQL_USERNAME:root}")
    private String mysqlUsername;

    @Value("${MYSQL_PASSWORD:}")
    private String mysqlPassword;

    @GetMapping
    public ResponseEntity<?> ejecutarModelo() {

        try {

            Path script = resolverScript();

            if (script == null) {
                return ResponseEntity.internalServerError().body(Map.of(
                        "success", false,
                        "error", "No se encontró el script de optimización (" + SCRIPT_RELATIVE_PATH
                                + "). Directorio de trabajo: " + System.getProperty("user.dir")));
            }

            String python = resolverPython(script);

            // Se ejecuta con la ruta absoluta para que el resultado no dependa
            // del directorio de trabajo desde el que arranca la aplicación.
            ProcessBuilder pb = new ProcessBuilder(python, script.toString());

            // Se inyectan las credenciales resueltas por Spring al entorno
            // del hijo, sobrescribiendo cualquier valor heredado.
            pb.environment().put("MYSQL_HOST", mysqlHost);
            pb.environment().put("MYSQL_PORT", mysqlPort);
            pb.environment().put("MYSQL_DATABASE", mysqlDatabase);
            pb.environment().put("MYSQL_USERNAME", mysqlUsername);
            pb.environment().put("MYSQL_PASSWORD", mysqlPassword);

            // Python escribe en la codificación de la consola (cp1252 en Windows)
            // al usar un pipe; se fuerza UTF-8 para coincidir con la lectura en Java.
            pb.environment().put("PYTHONUTF8", "1");
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            Process process = pb.start();

            // stdout y stderr se leen en paralelo: si no, el buffer de stderr
            // puede llenarse y bloquear el proceso.
            final StringBuilder stderr = new StringBuilder();
            Thread hiloError = new Thread(
                    () -> stderr.append(leerSalida(process.getErrorStream())),
                    "optimizacion-stderr");
            hiloError.start();

            String stdout = leerSalida(process.getInputStream());

            hiloError.join();
            int exitCode = process.waitFor();

            log.info("Optimización ejecutada (exit={}): script={} python={}", exitCode, script, python);

            if (exitCode != 0) {
                return ResponseEntity.internalServerError().body(Map.of(
                        "success", false,
                        "error", "El script Python terminó con error (exit " + exitCode + "): "
                                + detalle(stderr.toString(), stdout)));
            }

            if (stdout.isBlank()) {
                return ResponseEntity.internalServerError().body(Map.of(
                        "success", false,
                        "error", "El script Python no devolvió ningún resultado"));
            }

            JsonNode resultado;
            try {
                resultado = objectMapper.readTree(stdout);
            } catch (Exception e) {
                // Así nunca se envía basura al navegador: el cliente recibirá un
                // error descriptivo en lugar de un SyntaxError de JSON.parse.
                return ResponseEntity.internalServerError().body(Map.of(
                        "success", false,
                        "error", "La salida del script no es JSON válido: " + detalle(stdout, stderr.toString())));
            }

            return ResponseEntity.ok(Map.of("resultado", resultado));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "Ejecución interrumpida"));

        } catch (Exception e) {
            log.error("Error ejecutando el modelo de optimización", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", String.valueOf(e.getMessage())));
        }
    }

    /**
     * Localiza el script de optimización sin depender del directorio de trabajo.
     *
     * <p>Se recorren los directorios ascendentes a partir del directorio de trabajo
     * (cubre tanto la raíz del proyecto como {@code src/}), de modo que rutas
     * relativas como {@code ..\Modelo Matematico\...} resuelvan correctamente.
     *
     * @return la ruta absoluta del script, o {@code null} si no existe.
     */
    private Path resolverScript() {
        if (scriptPath != null && !scriptPath.isBlank()) {
            Path personalizado = Paths.get(scriptPath);
            if (personalizado.isAbsolute()) {
                return Files.isRegularFile(personalizado) ? personalizado.toAbsolutePath().normalize() : null;
            }
            return buscarEnAscendentes(personalizado);
        }
        return buscarEnAscendentes(Paths.get(SCRIPT_RELATIVE_PATH));
    }

    private Path buscarEnAscendentes(Path relativo) {
        Path base = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (base != null) {
            Path candidato = base.resolve(relativo);
            if (Files.isRegularFile(candidato)) {
                return candidato;
            }
            base = base.getParent();
        }
        return null;
    }

    /**
     * Elige el intérprete Python: la configuración explícita, si no el venv
     * que vive junto al script, y en último caso {@code python} del PATH.
     */
    private String resolverPython(Path script) {
        if (pythonCommand != null && !pythonCommand.isBlank()) {
            return pythonCommand;
        }
        Path carpeta = script.getParent();
        Path[] candidatos = {
                carpeta.resolve(".venv").resolve("Scripts").resolve("python.exe"),
                carpeta.resolve(".venv").resolve("bin").resolve("python")
        };
        for (Path candidato : candidatos) {
            if (Files.isRegularFile(candidato)) {
                return candidato.toString();
            }
        }
        return "python";
    }

    private String leerSalida(InputStream stream) {
        StringBuilder salida = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (salida.length() > 0) {
                    salida.append('\n');
                }
                salida.append(line);
            }
        } catch (IOException e) {
            log.warn("No se pudo leer la salida del proceso Python: {}", e.getMessage());
        }
        return salida.toString();
    }

    private String detalle(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            if (parte == null || parte.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(parte.trim());
        }
        String texto = sb.toString();
        if (texto.length() > MAX_DETALLE) {
            return texto.substring(0, MAX_DETALLE) + "...";
        }
        return texto;
    }
}
