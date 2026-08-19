package com.beautyManager.beautyManagerApi.service.businessHoursService;

import com.beautyManager.beautyManagerApi.dto.config.BusinessHoursRequestDTO;
import com.beautyManager.beautyManagerApi.dto.config.BusinessHoursResponseDTO;
import com.beautyManager.beautyManagerApi.entity.BusinessHoursEntity;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.BusinessHoursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessHoursServiceImpl implements BusinessHoursService {

    private final BusinessHoursRepository businessHoursRepository;

    @Override
    public List<BusinessHoursResponseDTO> findByBusinessId(UUID businessId) {
        return businessHoursRepository.findByBusinessIdOrderByDayAsc(businessId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BusinessHoursResponseDTO upsert(UUID businessId, BusinessHoursRequestDTO dto) {
        // Si existe el día, actualizamos; si no, creamos uno nuevo
        BusinessHoursEntity entity = businessHoursRepository
                .findByBusinessIdAndDay(businessId, dto.getDay())
                .orElseGet(() -> BusinessHoursEntity.builder()
                        .businessId(businessId)
                        .day(dto.getDay())
                        .build());

        entity.setOpensAt(dto.getOpensAt());
        entity.setClosesAt(dto.getClosesAt());
        entity.setIsClosed(Boolean.TRUE.equals(dto.getIsClosed()));

        return toResponse(businessHoursRepository.save(entity));
    }

    private BusinessHoursResponseDTO toResponse(BusinessHoursEntity e) {
        return BusinessHoursResponseDTO.builder()
                .id(e.getId())
                .businessId(e.getBusinessId())
                .day(e.getDay())
                .opensAt(e.getOpensAt())
                .closesAt(e.getClosesAt())
                .isClosed(e.getIsClosed())
                .build();
    }
}