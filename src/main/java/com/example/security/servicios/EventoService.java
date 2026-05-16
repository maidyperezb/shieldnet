package com.example.security.servicios;

import com.example.security.modelo.Evento;
import com.example.security.dto.EventoDTO;
import com.example.security.modelo.Usuario;
import com.example.security.repositorio.EventoRepository;
import com.example.security.repositorio.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HttpServletRequest request;

    public List<Evento> findAll() {
        return eventoRepository.findAllByOrderByFechaDescHoraDesc();
    }

    /**
     * Método principal — úsalo desde cualquier Service o Controller.
     * Obtiene el usuario autenticado automáticamente.
     */
    public Evento registrarEvento(String descripcion) {
    Evento evento = new Evento();

    String ip = request.getHeader("X-FORWARDED-FOR");
    if (ip == null) ip = request.getRemoteAddr();
    evento.setIp(ip);
    evento.setFecha(new Date());
    evento.setHora(new Date());
    evento.setDescripcion(descripcion);

    try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("=== EVENTO SERVICE ===");
        System.out.println("Descripcion: " + descripcion);
        System.out.println("Auth: " + auth);
        System.out.println("Auth name: " + (auth != null ? auth.getName() : "NULL"));
        
        if (auth != null && auth.isAuthenticated()
                && !auth.getPrincipal().equals("anonymousUser")) {
            Usuario usuario = usuarioRepository.findByUsername(auth.getName()).orElse(null);
            System.out.println("Usuario encontrado: " + usuario);
            if (usuario != null) evento.setUsuario(usuario);
        }
    } catch (Exception e) {
        System.out.println("Error obteniendo usuario: " + e.getMessage());
    }

    Evento saved = eventoRepository.save(evento);
    System.out.println("Evento guardado con ID: " + saved.getId());
    return saved;
}

    /**
     * Mantener compatibilidad con el método anterior que recibía EventoDTO
     */
    public Evento registrarEvento(EventoDTO eventoDTO, Usuario usuarioAutenticado) {
        return registrarEvento(eventoDTO.getDescripcion());
    }
}