package com.example.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                         DaoAuthenticationProvider authenticationProvider,
                                         FiltroUsuarioInactivo filtroUsuarioInactivo) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/login", "/registrar", "/home", "/", "/favicon.ico").permitAll()
                // Recursos estáticos (consolidados)
                .requestMatchers( "/img/**", "/css/**", "/js/**").permitAll()
                .requestMatchers("/api/dispositivos").permitAll()
                // APIs públicas
                .requestMatchers("/api/dispositivos").permitAll()
                .requestMatchers("/api/voip/**").permitAll()
                // Rutas de administrador
                .requestMatchers("/admin/interfaces").permitAll() // Específica pública
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Rutas de usuario
                .requestMatchers("/user/**").hasRole("USER")
                // Gestión de estados de usuarios y MACs: exclusiva de administradores
                // (desactivar/reactivar cambia estado; POST /api/macs puede reactivar
                // una MAC existente y sincroniza el estado de los dispositivos)
                .requestMatchers("/api/usuarios/*/desactivar", "/api/usuarios/*/reactivar",
                                 "/api/macs/*/desactivar", "/api/macs/*/reactivar").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/macs").hasRole("ADMIN")
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // Antes de autorizar: las cuentas desactivadas no usan sus sesiones abiertas
            .addFilterBefore(filtroUsuarioInactivo, org.springframework.security.web.access.intercept.AuthorizationFilter.class)
            .authenticationProvider(authenticationProvider);
            
        return http.build();
    }
    
}