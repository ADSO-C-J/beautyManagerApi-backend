package com.beautyManager.beautyManagerApi.service.clientPreferenceService;

import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceRequestDTO;
import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceResponseDTO;
import com.beautyManager.beautyManagerApi.dto.clientPreferenceDto.ClientPreferenceUpdateDTO;
import com.beautyManager.beautyManagerApi.entity.ClientEntity;
import com.beautyManager.beautyManagerApi.entity.ClientPreferenceEntity;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.ClientPreferenceRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientPreferenceServiceImpl implements ClientPreferenceService {

    private final ClientPreferenceRepository clientPreferenceRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClientPreferenceResponseDTO> findByClient(UUID clientId) {
        ensureClientExists(clientId);
        return clientPreferenceRepository.findAllByClientId(clientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientPreferenceResponseDTO findById(UUID id) {
        return toDTO(findPreferenceOrThrow(id));
    }

    @Override
    @Transactional
    public ClientPreferenceResponseDTO create(UUID clientId, ClientPreferenceRequestDTO dto) {
        ensureClientExists(clientId);

        String key = dto.getKey().trim();
        if (clientPreferenceRepository.existsByClientIdAndKey(clientId, key)) {
            throw new IllegalArgumentException("El cliente ya tiene una preferencia con la clave: " + key);
        }

        ClientPreferenceEntity preference = ClientPreferenceEntity.builder()
                .clientId(clientId)
                .key(key)
                .value(dto.getValue().trim())
                .build();

        return toDTO(clientPreferenceRepository.save(preference));
    }

    @Override
    @Transactional
    public ClientPreferenceResponseDTO update(UUID id, ClientPreferenceUpdateDTO dto) {
        ClientPreferenceEntity preference = findPreferenceOrThrow(id);
        preference.setValue(dto.getValue().trim());
        return toDTO(clientPreferenceRepository.save(preference));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ClientPreferenceEntity preference = findPreferenceOrThrow(id);
        // La tabla no tiene deleted_at: borrado físico
        clientPreferenceRepository.delete(preference);
    }

    private ClientPreferenceEntity findPreferenceOrThrow(UUID id) {
        return clientPreferenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Preferencia de cliente no encontrada con id: " + id));
    }

    private void ensureClientExists(UUID clientId) {
        clientRepository.findByIdAndDeletedAtIsNull(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + clientId));
    }

    private ClientPreferenceResponseDTO toDTO(ClientPreferenceEntity preference) {
        ClientEntity client = clientRepository.findById(preference.getClientId()).orElse(null);

        return ClientPreferenceResponseDTO.builder()
                .id(preference.getId())
                .clientId(preference.getClientId())
                .clientName(client != null ? client.getName() : null)
                .key(preference.getKey())
                .value(preference.getValue())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}