package com.example.security.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.security.modelo.Usuario;
import com.example.security.repositorio.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Bloquea las peticiones de cuentas autenticadas que están desactivadas.
 *
 * <p>Desactivar una cuenta impide nuevas autenticaciones (ver
 * {@code CustomUserDetailsService}), pero las sesiones ya abiertas seguirían
 * autenticadas. Este filtro las invalida y deniega la petición: 401 para
 * recursos API y redirección al login para páginas. Es la mínima intervención
 * necesaria: no registra sesiones ni introduce un {@code SessionRegistry}.</p>
 */
@Component
public class FiltroUsuarioInactivo extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public FiltroUsuarioInactivo(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (esAnonimo(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Usuario> usuario = usuarioRepository.findByUsername(authentication.getName());
        if (usuario.isPresent() && usuario.get().isActivo()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Cuenta desactivada (o inexistente): cerrar sesión y denegar el acceso
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        String uri = request.getRequestURI();
        if (uri.startsWith(request.getContextPath() + "/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Cuenta desactivada\"}");
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    private boolean esAnonimo(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal());
    }
}
