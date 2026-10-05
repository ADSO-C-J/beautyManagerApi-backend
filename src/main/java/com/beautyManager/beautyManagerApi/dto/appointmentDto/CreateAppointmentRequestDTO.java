package com.beautyManager.beautyManagerApi.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class CreateAppointmentRequestDTO {
    private UUID clientId;
    private UUID staffId;
    private String service;
    private String date;
    private String time;
    private String notes;
}