package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.ClientResponseDTO;
import com.beautyManager.beautyManagerApi.service.clientService.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> search(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(clientService.search(search));
    }
}