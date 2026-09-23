package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.StaffServiceEntity;
import com.beautyManager.beautyManagerApi.entity.StaffServiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StaffServiceRepository extends JpaRepository<StaffServiceEntity, StaffServiceId> {

    List<StaffServiceEntity> findAllByIdStaffId(UUID staffId);

    List<StaffServiceEntity> findAllByIdServiceId(UUID serviceId);

    boolean existsByIdStaffIdAndIdServiceId(UUID staffId, UUID serviceId);
}