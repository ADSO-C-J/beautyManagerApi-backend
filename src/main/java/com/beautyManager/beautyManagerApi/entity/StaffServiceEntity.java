package com.beautyManager.beautyManagerApi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Mapea la tabla puente staff_services: qué servicios puede realizar cada estilista.
 * La clave es compuesta (staff_id, service_id); no tiene columna id propia.
 */
@Entity
@Table(name = "staff_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffServiceEntity {

    @EmbeddedId
    private StaffServiceId id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}