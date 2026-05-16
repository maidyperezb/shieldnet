package com.example.security.controller;

import com.example.security.modelo.Evento;
import com.example.security.dto.EventoDTO;
import com.example.security.servicios.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @GetMapping
    public ResponseEntity<List<Evento>> getAllEvents() {
        List<Evento> eventos = eventoService.findAll();
        return new ResponseEntity<>(eventos, HttpStatus.OK);
    }

    /**
     * Corregido: ya no recibe Usuario como parámetro.
     * EventoService obtiene el usuario autenticado internamente.
     */
    @PostMapping("/registre")
    public ResponseEntity<Evento> registrarEvento(@RequestBody EventoDTO eventoDTO) {
        Evento nuevoEvento = eventoService.registrarEvento(eventoDTO.getDescripcion());
        return new ResponseEntity<>(nuevoEvento, HttpStatus.CREATED);
    }
}