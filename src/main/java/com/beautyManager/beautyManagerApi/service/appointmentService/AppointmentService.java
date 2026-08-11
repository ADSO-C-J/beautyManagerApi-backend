package com.beautyManager.beautyManagerApi.service.appointmentService;

import com.beautyManager.beautyManagerApi.dto.AppointmentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateAppointmentRequestDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    List<AppointmentResponseDTO> findAll(LocalDateTime start, LocalDateTime end);
    AppointmentResponseDTO create(CreateAppointmentRequestDTO dto);
}