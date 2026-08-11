package com.beautyManager.beautyManagerApi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "business_id", nullable = false)
    private UUID businessId;
    @Column(name = "client_id", nullable = false)
    private UUID clientId;
    @Column(name = "staff_id")
    private UUID staffId;
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;
    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;
    private String status;
    private String notes;
    @Column(name = "cancellation_reason")
    private String cancellationReason;
    @Column(name = "cancelled_by")
    private UUID cancelledBy;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}