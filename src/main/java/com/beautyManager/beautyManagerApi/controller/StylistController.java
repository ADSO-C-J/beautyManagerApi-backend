package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.StylistResponseDTO;
import com.beautyManager.beautyManagerApi.service.stylistService.StylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stylists")
@RequiredArgsConstructor
public class StylistController {

    private final StylistService stylistService;

    @GetMapping
    public ResponseEntity<List<StylistResponseDTO>> findAll() {
        return ResponseEntity.ok(stylistService.findAll());
    }
}