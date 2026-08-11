package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, UUID> {
    List<StaffEntity> findAllByIsActiveTrue();
    List<StaffEntity> findAllByBusinessIdAndIsActiveTrue(UUID businessId);
}