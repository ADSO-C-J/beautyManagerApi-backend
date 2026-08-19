package com.beautyManager.beautyManagerApi.dto.config;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BusinessResponseDTO {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String phone;
    private String email;
    private String website;
    private String logoUrl;
    private String currency;
    private String timezone;
}