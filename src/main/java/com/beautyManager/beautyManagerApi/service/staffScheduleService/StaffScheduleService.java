package com.beautyManager.beautyManagerApi.service.staffScheduleService;

import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffScheduleDto.StaffScheduleUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface StaffScheduleService {

    List<StaffScheduleResponseDTO> findAll();

    List<StaffScheduleResponseDTO> findByStaffId(UUID staffId);

    StaffScheduleResponseDTO findById(UUID id);

    StaffScheduleResponseDTO create(StaffScheduleRequestDTO dto);

    StaffScheduleResponseDTO update(UUID id, StaffScheduleUpdateDTO dto);

    void delete(UUID id);
}