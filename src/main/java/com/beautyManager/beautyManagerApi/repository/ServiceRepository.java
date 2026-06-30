package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findAllByDeletedAtIsNull();

    Optional<ServiceEntity> findByIdAndDeletedAtIsNull(UUID id);

}
