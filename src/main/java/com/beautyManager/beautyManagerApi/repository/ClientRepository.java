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

    List<ClientEntity> findAllByBusinessIdAndDeletedAtIsNull(UUID businessId);
    List<ClientEntity> findAllByBusinessIdAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID businessId, String name);
}