package com.beautyManager.beautyManagerApi.service.businessService;

import com.beautyManager.beautyManagerApi.dto.config.BusinessRequestDTO;
import com.beautyManager.beautyManagerApi.dto.config.BusinessResponseDTO;
import com.beautyManager.beautyManagerApi.entity.BusinessesEntity;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;

    @Override
    public BusinessResponseDTO get() {
        BusinessesEntity business = findDefault();
        return toResponse(business);
    }

    @Override
    public BusinessResponseDTO update(BusinessRequestDTO dto) {
        BusinessesEntity business = findDefault();

        business.setName(dto.getName());
        business.setAddress(dto.getAddress());
        business.setCity(dto.getCity());
        business.setCountry(dto.getCountry());
        business.setPhone(dto.getPhone());
        business.setEmail(dto.getEmail());
        business.setWebsite(dto.getWebsite());
        business.setLogo_url(dto.getLogoUrl());
        business.setCurrency(dto.getCurrency());
        business.setTimezone(dto.getTimezone() != null ? ZoneId.of(dto.getTimezone()) : business.getTimezone());

        return toResponse(businessRepository.save(business));
    }

    private BusinessesEntity findDefault() {
        return businessRepository.findAllOrderedByCreation().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No existe un negocio configurado"));
    }

    private BusinessResponseDTO toResponse(BusinessesEntity b) {
        return BusinessResponseDTO.builder()
                .id(b.getId())
                .name(b.getName())
                .address(b.getAddress())
                .city(b.getCity())
                .country(b.getCountry())
                .phone(b.getPhone())
                .email(b.getEmail())
                .website(b.getWebsite())
                .logoUrl(b.getLogo_url())
                .currency(b.getCurrency())
                .timezone(b.getTimezone() != null ? b.getTimezone().toString() : null)
                .build();
    }
}