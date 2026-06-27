package com.beautyManager.beautyManagerApi.service.serviceService;

import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceRequestDTO;
import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceResponseDTO;
import com.beautyManager.beautyManagerApi.entity.ServiceEntity;
import com.beautyManager.beautyManagerApi.enums.TypeServices;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {
    private final ServiceRepository serviceRepository;

    private ServiceResponseDTO toDTO(ServiceEntity service) {
        ServiceResponseDTO dto = new ServiceResponseDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setPrice(service.getPrice());
        dto.setDescription(service.getDescription());
        dto.setDuration_min(service.getDurationMin());
        return dto;
    }


    @Override
    public List<ServiceResponseDTO> findAll() {
        return serviceRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceResponseDTO findById(UUID id) {
        ServiceEntity service = serviceRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con id: " + id));

        return toDTO(service);
    }

    @Override
    public ServiceResponseDTO create(ServiceRequestDTO dto) {

        ServiceEntity service = ServiceEntity.builder()
                .businessId(dto.getBusinessId())
                .name(dto.getName())
                .description(dto.getDescription())
                .category(TypeServices.valueOf(dto.getCategory()))
                .durationMin(dto.getDuration_min())
                .price(dto.getPrice())
                .isActive(true)
                .isPopular(true)
                .displayOrder(0)
                .build();

        return toDTO(serviceRepository.save(service));
    }

    @Override
    public ServiceResponseDTO update(UUID id, ServiceRequestDTO dto) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }

}
