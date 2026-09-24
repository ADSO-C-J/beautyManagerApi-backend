package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.ClientNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientNoteRepository extends JpaRepository<ClientNoteEntity, UUID> {

    List<ClientNoteEntity> findAllByClientIdAndDeletedAtIsNull(UUID clientId);

    List<ClientNoteEntity> findAllByStaffIdAndDeletedAtIsNull(UUID staffId);

    Optional<ClientNoteEntity> findByIdAndDeletedAtIsNull(UUID id);
}