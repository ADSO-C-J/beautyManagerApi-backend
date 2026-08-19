package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.BusinessHoursEntity;
import com.beautyManager.beautyManagerApi.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessHoursRepository extends JpaRepository<BusinessHoursEntity, UUID> {

    List<BusinessHoursEntity> findByBusinessIdOrderByDayAsc(UUID businessId);

    Optional<BusinessHoursEntity> findByBusinessIdAndDay(UUID businessId, DayOfWeek day);
}