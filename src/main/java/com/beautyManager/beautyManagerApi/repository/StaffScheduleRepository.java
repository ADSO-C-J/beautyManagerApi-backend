package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.StaffScheduleEntity;
import com.beautyManager.beautyManagerApi.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffScheduleEntity, UUID> {

    List<StaffScheduleEntity> findByStaffIdOrderByDayAsc(UUID staffId);

    List<StaffScheduleEntity> findAllByIsActiveTrue();

    Optional<StaffScheduleEntity> findByStaffIdAndDay(UUID staffId, DayOfWeek day);
}