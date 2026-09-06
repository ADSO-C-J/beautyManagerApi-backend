package com.beautyManager.beautyManagerApi.entity;

import com.beautyManager.beautyManagerApi.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "staff_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "day", nullable = false, columnDefinition = "day_of_week")
    private DayOfWeek day;

    @Column(name = "starts_at", nullable = false, columnDefinition = "time")
    private LocalTime startsAt;

    @Column(name = "ends_at", nullable = false, columnDefinition = "time")
    private LocalTime endsAt;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}