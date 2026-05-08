package com.example.security.repositorio;

import com.example.security.modelo.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestionar conversaciones del chat AI.
 */
@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    /**
     * Obtener conversaciones ACTIVAS de un usuario ordenadas por fecha descendente (más
     * recientes primero) - Excluye eliminadas lógicamente
     */
    @Query("SELECT c FROM Conversacion c WHERE c.usuario.id = :usuarioId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Conversacion> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

    /**
     * Obtener últimas N conversaciones ACTIVAS de un usuario
     */
    @Query("SELECT c FROM Conversacion c WHERE c.usuario.id = :usuarioId AND c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Conversacion> findRecentConversations(Long usuarioId, int limit);

    /**
     * Marcar como eliminadas (soft delete) todas las conversaciones ACTIVAS de un usuario
     */
    @Modifying
    @Transactional
    @Query("UPDATE Conversacion c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.usuario.id = :usuarioId AND c.deletedAt IS NULL")
    void softDeleteByUsuarioId(Long usuarioId);

    /**
     * Encontrar conversaciones ACTIVAS anteriores a una fecha (para limpieza de datos)
     */
    @Query("SELECT c FROM Conversacion c WHERE c.createdAt < :date AND c.deletedAt IS NULL")
    List<Conversacion> findByCreatedAtBefore(LocalDateTime date);

    /**
     * Contar conversaciones ACTIVAS de un usuario
     */
    @Query("SELECT COUNT(c) FROM Conversacion c WHERE c.usuario.id = :usuarioId AND c.deletedAt IS NULL")
    long countByUsuarioId(Long usuarioId);
}
