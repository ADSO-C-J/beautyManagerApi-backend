package com.beautyManager.beautyManagerApi.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class StylistResponseDTO {
    /** Id del usuario (users.id). */
    private UUID id;
    /** Id del registro de staff (staff.id); es el que usan los endpoints /api/staff/{staffId}/... */
    private UUID staffId;
    private String name;
    private String specialty;
    private String avatarUrl;
}