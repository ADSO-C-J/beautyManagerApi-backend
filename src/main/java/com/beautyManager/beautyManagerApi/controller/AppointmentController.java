package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.AppointmentResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateAppointmentRequestDTO;
import com.beautyManager.beautyManagerApi.service.appointmentService.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> findAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo) {
        return ResponseEntity.ok(appointmentService.findAll(dateFrom, dateTo));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> create(@RequestBody CreateAppointmentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(dto));
    }
}