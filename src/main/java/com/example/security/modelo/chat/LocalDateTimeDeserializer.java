package com.example.security.modelo.chat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Deserializador personalizado para LocalDateTime que acepta múltiples
 * formatos: - "yyyy-MM-dd" (solo fecha) - "yyyy-MM-dd'T'HH:mm:ss" (fecha con
 * hora)
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_WITH_MILLIS = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // Intentar primero con fecha y hora con milisegundos
            if (value.length() > 19 && value.contains(".")) {
                return LocalDateTime.parse(value, DATE_TIME_WITH_MILLIS);
            }
            // Intentar con fecha y hora
            else if (value.length() > 10 && value.contains("T")) {
                return LocalDateTime.parse(value, DATE_TIME);
            }
            // Si es solo fecha, convertir a LocalDateTime al inicio del día
            else if (value.length() == 10) {
                return LocalDate.parse(value, DATE_ONLY).atStartOfDay();
            }
            // Fallback: intentar con formato ISO
            else {
                return LocalDateTime.parse(value);
            }
        }
        catch (Exception e) {
            throw new IOException("No se puede parsear LocalDateTime: " + value
                    + ". Formatos válidos: yyyy-MM-dd o yyyy-MM-dd'T'HH:mm:ss", e);
        }
    }
}
