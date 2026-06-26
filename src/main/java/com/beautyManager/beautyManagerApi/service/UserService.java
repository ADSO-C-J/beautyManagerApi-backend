package com.beautyManager.beautyManagerApi.service;

import com.beautyManager.beautyManagerApi.dto.UserRequestDTO;
import com.beautyManager.beautyManagerApi.dto.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    List<UserResponseDTO> findAll();
    UserResponseDTO findById(UUID id);
    UserResponseDTO create(UserRequestDTO dto);
    UserResponseDTO update(UUID id, UserRequestDTO dto);
    void delete(UUID id);
}