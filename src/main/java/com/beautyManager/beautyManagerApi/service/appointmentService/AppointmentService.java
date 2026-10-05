package com.beautyManager.beautyManagerApi.service.appointmentService;

import com.beautyManager.beautyManagerApi.dto.appointmentDto.AppointmentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.appointmentDto.CreateAppointmentRequestDTO;
import com.beautyManager.beautyManagerApi.dto.appointmentDto.UpdateAppointmentRequestDTO;


import java.time.LocalDateTime;
import java.util.List;
import  java.util.UUID;
public interface AppointmentService {
    List<AppointmentResponseDTO> findAll(LocalDateTime start, LocalDateTime end);
    AppointmentResponseDTO create(CreateAppointmentRequestDTO dto);
    AppointmentResponseDTO findById(UUID id);
    AppointmentResponseDTO update(UUID id, UpdateAppointmentRequestDTO dto);
    void delete(UUID id);
}