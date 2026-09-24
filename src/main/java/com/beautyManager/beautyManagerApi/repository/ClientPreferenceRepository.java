package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.ClientPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientPreferenceRepository extends JpaRepository<ClientPreferenceEntity, UUID> {

    List<ClientPreferenceEntity> findAllByClientId(UUID clientId);

    Optional<ClientPreferenceEntity> findByClientIdAndKey(UUID clientId, String key);

    boolean existsByClientIdAndKey(UUID clientId, String key);
}