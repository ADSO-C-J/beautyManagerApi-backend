package com.beautyManager.beautyManagerApi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapea la tabla client_notes: notas internas del equipo sobre un cliente.
 * Cada nota puede apuntar al miembro del personal que la escribió (staff_id nullable).
 */
@Entity
@Table(name = "client_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "staff_id")
    private UUID staffId;

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}