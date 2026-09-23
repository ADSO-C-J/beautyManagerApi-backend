package com.beautyManager.beautyManagerApi.service.staffServiceService;

import com.beautyManager.beautyManagerApi.dto.serviceDto.ServiceResponseDTO;
import com.beautyManager.beautyManagerApi.dto.staffServiceDto.StaffServiceAssignRequestDTO;
import com.beautyManager.beautyManagerApi.dto.staffServiceDto.StaffServiceResponseDTO;
import com.beautyManager.beautyManagerApi.entity.ServiceEntity;
import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import com.beautyManager.beautyManagerApi.entity.StaffServiceEntity;
import com.beautyManager.beautyManagerApi.entity.StaffServiceId;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.ServiceRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.StaffServiceRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffServiceServiceImpl implements StaffServiceService {

    private final StaffServiceRepository staffServiceRepository;
    private final StaffRepository staffRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StaffServiceResponseDTO> findByStaff(UUID staffId) {
        ensureStaffExists(staffId);
        return staffServiceRepository.findAllByIdStaffId(staffId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffServiceResponseDTO> findByService(UUID serviceId) {
        ensureServiceExists(serviceId);
        return staffServiceRepository.findAllByIdServiceId(serviceId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> findAvailableByStaff(UUID staffId) {
        ensureStaffExists(staffId);

        Set<UUID> assignedServiceIds = staffServiceRepository.findAllByIdStaffId(staffId).stream()
                .map(assignment -> assignment.getId().getServiceId())
                .collect(Collectors.toSet());

        return serviceRepository.findAllByDeletedAtIsNull().stream()
                .filter(service -> !assignedServiceIds.contains(service.getId()))
                .map(this::toServiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StaffServiceResponseDTO> assign(UUID staffId, StaffServiceAssignRequestDTO dto) {
        ensureStaffExists(staffId);

        List<UUID> serviceIds = new ArrayList<>(dto.getServiceIds());
        serviceIds.removeIf(Objects::isNull);
        serviceIds = serviceIds.stream().distinct().collect(Collectors.toList());

        if (serviceIds.isEmpty()) {
            throw new IllegalArgumentException("La lista de serviceIds no puede estar vacía");
        }

        for (UUID serviceId : serviceIds) {
            ensureServiceExists(serviceId);
            if (!staffServiceRepository.existsByIdStaffIdAndIdServiceId(staffId, serviceId)) {
                staffServiceRepository.save(StaffServiceEntity.builder()
                        .id(new StaffServiceId(staffId, serviceId))
                        .build());
            }
        }
        return findByStaff(staffId);
    }

    @Override
    @Transactional
    public void unassign(UUID staffId, UUID serviceId) {
        ensureStaffExists(staffId);
        ensureServiceExists(serviceId);

        StaffServiceId key = new StaffServiceId(staffId, serviceId);
        StaffServiceEntity assignment = staffServiceRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El servicio " + serviceId + " no está asignado al miembro del personal " + staffId));
        staffServiceRepository.delete(assignment);
    }

    private StaffServiceResponseDTO toDTO(StaffServiceEntity assignment) {
        UUID staffId = assignment.getId().getStaffId();
        UUID serviceId = assignment.getId().getServiceId();

        StaffEntity staff = staffRepository.findById(staffId).orElse(null);
        String staffName = staff != null
                ? userRepository.findById(staff.getUserId()).map(User::getName).orElse(null)
                : null;

        ServiceEntity service = serviceRepository.findById(serviceId).orElse(null);

        return StaffServiceResponseDTO.builder()
                .staffId(staffId)
                .staffName(staffName)
                .serviceId(serviceId)
                .serviceName(service != null ? service.getName() : null)
                .serviceCategory(service != null ? service.getCategory() : null)
                .servicePrice(service != null ? service.getPrice() : null)
                .serviceDurationMin(service != null ? service.getDurationMin() : null)
                .createdAt(assignment.getCreatedAt())
                .build();
    }

    private ServiceResponseDTO toServiceDTO(ServiceEntity service) {
        ServiceResponseDTO dto = new ServiceResponseDTO();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDuration_min(service.getDurationMin());
        dto.setPrice(service.getPrice());
        dto.setDescription(service.getDescription());
        return dto;
    }

    private void ensureStaffExists(UUID staffId) {
        staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro del personal no encontrado con id: " + staffId));
    }

    private void ensureServiceExists(UUID serviceId) {
        serviceRepository.findByIdAndDeletedAtIsNull(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado con id: " + serviceId));
    }
}