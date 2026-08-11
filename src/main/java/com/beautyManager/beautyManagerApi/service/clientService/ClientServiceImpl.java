package com.beautyManager.beautyManagerApi.service.clientService;

import com.beautyManager.beautyManagerApi.dto.ClientResponseDTO;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.enums.UserRole;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final UserRepository userRepository;

    @Override
    public List<ClientResponseDTO> search(String query) {
        List<User> users = userRepository.findAllByRoleAndDeletedAtIsNull(UserRole.cliente);
        if (query != null && !query.isBlank()) {
            String q = query.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> u.getName().toLowerCase().contains(q)
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ClientResponseDTO toDTO(User user) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        return dto;
    }
}