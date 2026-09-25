package com.beautyManager.beautyManagerApi.service.facialService;


import com.beautyManager.beautyManagerApi.dto.facialDto.*;
import com.beautyManager.beautyManagerApi.entity.FacialAnalysisEntity;
import com.beautyManager.beautyManagerApi.entity.FacialRecommendationEntity;
import com.beautyManager.beautyManagerApi.exception.ResourceNotFoundException;
import com.beautyManager.beautyManagerApi.repository.*;
import com.beautyManager.beautyManagerApi.repository.FacialAnalysisRepository;
import com.beautyManager.beautyManagerApi.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacialAnalysisServiceImpl implements FacialAnalysisService {

    private final FacialAnalysisRepository analysisRepository;
    private final FacialRecommendationRepository recommendationRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;   // ajusta al nombre real en tu proyecto
    private final StaffRepository staffRepository;  // ajusta al nombre real en tu proyecto

    @Override
    @Transactional(readOnly = true)
    public List<FacialAnalysisResponseDTO> findByClient(UUID clientId) {
        findActiveClientOrThrow(clientId);
        return analysisRepository.findAllByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FacialAnalysisResponseDTO findById(UUID id) {
        return toDTO(findAnalysisOrThrow(id));
    }

    @Override
    @Transactional
    public FacialAnalysisResponseDTO create(UUID clientId, FacialAnalysisRequestDTO dto) {
        findActiveClientOrThrow(clientId);

        FacialAnalysisEntity analysis = new FacialAnalysisEntity();
        analysis.setClientId(clientId);
        analysis.setStaffId(resolveCurrentStaffId());
        analysis.setImageUrl(dto.imageUrl());
        analysis.setSkinTone(dto.skinTone());
        analysis.setSkinToneHex(dto.skinToneHex());
        analysis.setHairType(dto.hairType());
        analysis.setFaceShape(dto.faceShape());
        analysis.setConfidencePct(dto.confidencePct());
        analysis.setRawResult(dto.rawResult());

        FacialAnalysisEntity saved = analysisRepository.save(analysis);

        if (dto.recommendations() != null) {
            dto.recommendations().forEach(recDto -> {
                FacialRecommendationEntity rec = new FacialRecommendationEntity();
                rec.setAnalysisId(saved.getId());
                rec.setCategory(recDto.category());
                rec.setTitle(recDto.title());
                rec.setDescription(recDto.description());
                recommendationRepository.save(rec);
            });
        }

        return toDTO(saved);
    }

    @Override
    @Transactional
    public FacialRecommendationResponseDTO addRecommendation(UUID analysisId, FacialRecommendationRequestDTO dto) {
        findAnalysisOrThrow(analysisId);

        FacialRecommendationEntity rec = new FacialRecommendationEntity();
        rec.setAnalysisId(analysisId);
        rec.setCategory(dto.category());
        rec.setTitle(dto.title());
        rec.setDescription(dto.description());

        FacialRecommendationEntity saved = recommendationRepository.save(rec);
        return toRecommendationDTO(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findAnalysisOrThrow(id);
        analysisRepository.deleteById(id); // cascade en BD borra las recomendaciones
    }

    // ---------- helpers privados ----------

    private FacialAnalysisEntity findAnalysisOrThrow(UUID id) {
        return analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Análisis facial no encontrado: " + id));
    }

    private void findActiveClientOrThrow(UUID clientId) {
        clientRepository.findByIdAndDeletedAtIsNull(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clientId));
    }

    /**
     * Mismo patrón que ClientNoteServiceImpl.resolveCurrentStaffId():
     * email del usuario autenticado -> User -> Staff. Null si no es staff.
     */
    private UUID resolveCurrentStaffId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .flatMap(user -> staffRepository.findByUserId(user.getId()))
                .map(Staff::getId) // ajusta al tipo real de tu entidad Staff
                .map(Staff::getId) // ajusta al tipo real de tu entidad Staff
                .orElse(null);
    }

    private FacialAnalysisResponseDTO toDTO(FacialAnalysisEntity entity) {
        List<FacialRecommendationResponseDTO> recs = recommendationRepository
                .findAllByAnalysisId(entity.getId())
                .stream()
                .map(this::toRecommendationDTO)
                .collect(Collectors.toList());

        String clientName = clientRepository.findById(entity.getClientId())
                .map(Client::getFullName) // ajusta al método real de Client
                .orElse(null);

        String staffName = entity.getStaffId() == null ? null :
                staffRepository.findById(entity.getStaffId())
                        .map(Staff::getFullName) // ajusta al método real de Staff
                        .orElse(null);

        return new FacialAnalysisResponseDTO(
                entity.getId(), entity.getClientId(), clientName,
                entity.getStaffId(), staffName, entity.getImageUrl(),
                entity.getSkinTone(), entity.getSkinToneHex(), entity.getHairType(),
                entity.getFaceShape(), entity.getConfidencePct(), entity.getRawResult(),
                entity.getCreatedAt(), recs
        );
    }

    private FacialRecommendationResponseDTO toRecommendationDTO(FacialRecommendationEntity entity) {
        return new FacialRecommendationResponseDTO(
                entity.getId(), entity.getCategory(), entity.getTitle(),
                entity.getDescription(), entity.getCreatedAt()
        );
    }
}