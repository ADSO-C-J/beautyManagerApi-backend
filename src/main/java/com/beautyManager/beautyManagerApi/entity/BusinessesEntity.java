package com.beautyManager.beautyManagerApi.entity;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "businesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    private UUID id;


    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;

    @Column(name = "address", columnDefinition = "text")
    private String address;

    @Column(name = "city", columnDefinition = "text")
    private String city;

    @Column(name = "country", nullable = false, length = 2, columnDefinition = "char(2)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String country;

    @Column(name = "phone", columnDefinition = "text")
    private String phone;

    @Column(name = "email", columnDefinition = "text")
    private String email;

    @Column(name = "website", columnDefinition = "text")
    private String website;

    @Column(name = "logo_url", columnDefinition = "text")
    private String logo_url;

    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "char(3)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;

    @Column(name = "timezone", nullable = false, columnDefinition = "text")
    private ZoneId timezone;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime created_at;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updated_at;

}
