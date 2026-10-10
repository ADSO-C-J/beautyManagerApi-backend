package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;


@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, UUID> {
    Optional<ClientEntity> findByIdAndDeletedAtIsNull(UUID id);

    /** Ficha de un usuario concreto (users.id -> clients.user_id); la usa la sincronización de usuarios. */
    Optional<ClientEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

    /**
     * Ficha de un usuario sin filtrar por borrado lógico. Hace falta porque
     * clients.user_id es UNIQUE: existe una fila (aunque esté borrada) y no se
     * puede crear otra.
     */
    Optional<ClientEntity> findByUserId(UUID userId);

    // Vista global: la usa el administrador, que no esta acotado a un business_id.
    List<ClientEntity> findAllByDeletedAtIsNull();

    List<ClientEntity> findAllByBusinessIdAndDeletedAtIsNull(UUID businessId);
    List<ClientEntity> findAllByBusinessIdAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID businessId, String name);

    long countByBusinessIdAndDeletedAtIsNullAndCreatedAtBetween(
            UUID businessId, java.time.LocalDateTime from, java.time.LocalDateTime to);
}