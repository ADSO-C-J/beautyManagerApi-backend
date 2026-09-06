package com.beautyManager.beautyManagerApi.service.reviewService;

import com.beautyManager.beautyManagerApi.dto.reviewDto.RatingStatsDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewRequestDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewResponseDTO;
import com.beautyManager.beautyManagerApi.dto.reviewDto.ReviewUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    List<ReviewResponseDTO> findAll();

    ReviewResponseDTO findById(UUID id);

    List<ReviewResponseDTO> findByAppointmentId(UUID appointmentId);

    List<ReviewResponseDTO> findByClientId(UUID clientId);

    List<ReviewResponseDTO> findByStaffId(UUID staffId);

    List<RatingStatsDTO> getRatingStats();

    ReviewResponseDTO create(ReviewRequestDTO dto);

    ReviewResponseDTO update(UUID id, ReviewUpdateDTO dto);

    void delete(UUID id);
}