package com.beautyManager.beautyManagerApi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "appointment_reminders", nullable = false)
    private Boolean appointmentReminders = true;

    @Column(name = "new_clients", nullable = false)
    private Boolean newClients = true;

    @Column(name = "cancellations", nullable = false)
    private Boolean cancellations = true;

    @Column(name = "monthly_reports", nullable = false)
    private Boolean monthlyReports = false;

    @Column(name = "system_updates", nullable = false)
    private Boolean systemUpdates = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}