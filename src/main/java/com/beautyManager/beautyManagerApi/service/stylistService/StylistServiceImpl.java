package com.beautyManager.beautyManagerApi.service.stylistService;

import com.beautyManager.beautyManagerApi.dto.StylistResponseDTO;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StylistServiceImpl implements StylistService {

    private final UserRepository userRepository;

    @Override
    public List<StylistResponseDTO> findAll() {
        return userRepository.findAllByRoleAndDeletedAtIsNull(UserRole.estilista)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private StylistResponseDTO toDTO(User user) {
        StylistResponseDTO dto = new StylistResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }
}