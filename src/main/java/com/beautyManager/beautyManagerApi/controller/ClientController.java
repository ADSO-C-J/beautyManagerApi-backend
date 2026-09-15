package com.beautyManager.beautyManagerApi.controller;

import com.beautyManager.beautyManagerApi.dto.ClientResponseDTO;
import com.beautyManager.beautyManagerApi.dto.CreateClientRequestDTO;
import com.beautyManager.beautyManagerApi.dto.UpdateClientRequestDTO;
import com.beautyManager.beautyManagerApi.service.clientService.ClientService;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Búsqueda de clientes (protegido con JWT)")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> search(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(clientService.search(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> findById(@PathVariable UUID id){
        return ResponseEntity.ok(clientService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody CreateClientRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateClientRequestDTO dto){
        return ResponseEntity.ok(clientService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
