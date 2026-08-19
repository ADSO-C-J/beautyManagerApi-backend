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
@Table(name = "business_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "day", nullable = false, columnDefinition = "day_of_week")
    private DayOfWeek day;

    @Column(name = "opens_at", columnDefinition = "time")
    private LocalTime opensAt;

    @Column(name = "closes_at", columnDefinition = "time")
    private LocalTime closesAt;

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}