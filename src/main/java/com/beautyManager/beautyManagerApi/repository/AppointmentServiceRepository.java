package com.beautyManager.beautyManagerApi.repository;

import com.beautyManager.beautyManagerApi.entity.AppointmentServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentServiceRepository extends JpaRepository<AppointmentServiceEntity, UUID> {

    List<AppointmentServiceEntity> findAllByAppointmentId(UUID appointmentId);

    List<AppointmentServiceEntity> findAllByServiceId(UUID serviceId);

    void deleteAllByAppointmentId(UUID appointmentId);
}
