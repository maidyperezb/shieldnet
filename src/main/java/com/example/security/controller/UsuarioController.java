package com.example.security.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.security.dto.UsuarioDTO;
import com.example.security.modelo.Usuario;
import com.example.security.servicios.EventoService;
import com.example.security.servicios.UserService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventoService eventoService;

    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = userService.findAll();
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/crear")
    public ResponseEntity<Usuario> createUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        try {
            Usuario nuevoUsuario = userService.save(usuarioDTO);
            eventoService.registrarEvento(
                "Creación de usuario: " + usuarioDTO.getUsername()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Desactiva un usuario sin eliminar su registro. El usuario deja de poder
     * iniciar sesión; su historial y relaciones se conservan. La operación es
     * reversible mediante PATCH /{id}/reactivar.
     */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long id) {
        try {
            Usuario usuario = userService.desactivarUsuario(id);
            eventoService.registrarEvento(
                "Usuario desactivado: " + usuario.getUsername() + " (ID: " + id + ")"
            );
            return ResponseEntity.ok(usuario);
        } catch (IllegalStateException e) {
            // Transición inválida: ya estaba inactivo
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Reactiva un usuario previamente desactivado. Recupera el acceso al
     * inicio de sesión conservando el mismo registro, credenciales y roles.
     */
    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivarUsuario(@PathVariable Long id) {
        try {
            Usuario usuario = userService.reactivarUsuario(id);
            eventoService.registrarEvento(
                "Usuario reactivado: " + usuario.getUsername() + " (ID: " + id + ")"
            );
            return ResponseEntity.ok(usuario);
        } catch (IllegalStateException e) {
            // Transición inválida: ya estaba activo
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Long id) {
        try {
            Usuario usuario = userService.findById(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuario no encontrado con ID: " + id);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUsuarioAutenticado(Principal principal) {
        try {
            Usuario usuario = userService.findByUsername(principal.getName());
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuario no encontrado");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuarioPorId(
            @PathVariable Long id,
            @RequestBody UsuarioDTO usuarioDTO) {
        try {
            Usuario usuarioExistente = userService.findById(id);

            usuarioExistente.setUsername(usuarioDTO.getUsername());
            usuarioExistente.setNombre(usuarioDTO.getNombre());
            usuarioExistente.setApellido(usuarioDTO.getApellido());
            usuarioExistente.setEmail(usuarioDTO.getEmail());

            if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().trim().isEmpty()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
            }

            if (usuarioDTO.getRoles() != null && !usuarioDTO.getRoles().isEmpty()) {
                Set<String> roles = new HashSet<>(usuarioDTO.getRoles());
                usuarioExistente.setRoles(roles);
            }

            Usuario usuarioActualizado = userService.saveExisting(usuarioExistente);
            eventoService.registrarEvento(
                "Edición de usuario: " + usuarioDTO.getUsername() + " (ID: " + id + ")"
            );
            return ResponseEntity.ok(usuarioActualizado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Usuario no encontrado con ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al actualizar usuario: " + e.getMessage());
        }
    }
}