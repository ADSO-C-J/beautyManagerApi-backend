package com.beautyManager.beautyManagerApi.service.clientNoteService;

import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteRequestDTO;
import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteResponseDTO;
import com.beautyManager.beautyManagerApi.dto.clientNoteDto.ClientNoteUpdateDTO;
import com.beautyManager.beautyManagerApi.entity.ClientEntity;
import com.beautyManager.beautyManagerApi.entity.ClientNoteEntity;
import com.beautyManager.beautyManagerApi.entity.StaffEntity;
import com.beautyManager.beautyManagerApi.entity.User;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.ClientNoteRepository;
import com.beautyManager.beautyManagerApi.repository.ClientRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import com.beautyManager.beautyManagerApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientNoteServiceImpl implements ClientNoteService {

    private final ClientNoteRepository clientNoteRepository;
    private final ClientRepository clientRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClientNoteResponseDTO> findByClient(UUID clientId) {
        ensureClientExists(clientId);
        return clientNoteRepository.findAllByClientIdAndDeletedAtIsNull(clientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClientNoteResponseDTO findById(UUID id) {
        return toDTO(findActiveNoteOrThrow(id));
    }

    @Override
    @Transactional
    public ClientNoteResponseDTO create(UUID clientId, ClientNoteRequestDTO dto) {
        ensureClientExists(clientId);

        ClientNoteEntity note = ClientNoteEntity.builder()
                .clientId(clientId)
                .staffId(resolveCurrentStaffId())
                .content(dto.getContent().trim())
                .build();

        return toDTO(clientNoteRepository.save(note));
    }

    @Override
    @Transactional
    public ClientNoteResponseDTO update(UUID id, ClientNoteUpdateDTO dto) {
        ClientNoteEntity note = findActiveNoteOrThrow(id);
        note.setContent(dto.getContent().trim());
        return toDTO(clientNoteRepository.save(note));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        ClientNoteEntity note = findActiveNoteOrThrow(id);
        note.setDeletedAt(LocalDateTime.now()); // borrado lógico, consistente con el resto del proyecto
        clientNoteRepository.save(note);
    }

    private ClientNoteEntity findActiveNoteOrThrow(UUID id) {
        return clientNoteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nota de cliente no encontrada con id: " + id));
    }

    private void ensureClientExists(UUID clientId) {
        clientRepository.findByIdAndDeletedAtIsNull(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + clientId));
    }

    /**
     * Resuelve el staff_id del usuario autenticado, o null si no es miembro del personal.
     * El mismo patrón que resolveBusinessId() en ClientServiceImpl.
     */
    private UUID resolveCurrentStaffId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        return userRepository.findByEmailAndDeletedAtIsNull(auth.getName())
                .flatMap(user -> staffRepository.findByUserId(user.getId()))
                .map(StaffEntity::getId)
                .orElse(null);
    }

    private ClientNoteResponseDTO toDTO(ClientNoteEntity note) {
        ClientEntity client = clientRepository.findById(note.getClientId()).orElse(null);

        String staffName = null;
        if (note.getStaffId() != null) {
            staffName = staffRepository.findById(note.getStaffId())
                    .flatMap(staff -> userRepository.findById(staff.getUserId()))
                    .map(User::getName)
                    .orElse(null);
        }

        return ClientNoteResponseDTO.builder()
                .id(note.getId())
                .clientId(note.getClientId())
                .clientName(client != null ? client.getName() : null)
                .staffId(note.getStaffId())
                .staffName(staffName)
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}