package com.beautyManager.beautyManagerApi.entity;

import com.beautyManager.beautyManagerApi.enums.ClientFrequency;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "business_id", nullable = false)
    private UUID businessId;
    @Column(nullable = false)
    private String name;
    private String email;
    private String phone;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    private String address;
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ClientFrequency frequency;
    @Column(name = "total_visits", nullable = false)
    private Integer totalVisits;
    @Column(name = "total_spent", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalSpent;
    @Column(name = "last_visit_at")
    private LocalDate lastVisitAt;
    private String notes;
    @Column(name = "is_active")
    private Boolean isActive;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}