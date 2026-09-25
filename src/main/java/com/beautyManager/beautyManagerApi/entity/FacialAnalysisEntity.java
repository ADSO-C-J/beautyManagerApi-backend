package com.beautyManager.beautyManagerApi.entity;

import com.beautyManager.beautyManagerApi.enums.FaceShape;
import com.beautyManager.beautyManagerApi.enums.HairType;
import com.beautyManager.beautyManagerApi.enums.SkinTone;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "facial_analyses")
public class FacialAnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "staff_id")
    private UUID staffId;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "skin_tone", columnDefinition = "skin_tone")
    private SkinTone skinTone;

    @Column(name = "skin_tone_hex", columnDefinition = "CHAR(7)")
    private String skinToneHex;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "hair_type", columnDefinition = "hair_type")
    private HairType hairType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "face_shape", columnDefinition = "face_shape")
    private FaceShape faceShape;

    @Column(name = "confidence_pct", precision = 5, scale = 2)
    private BigDecimal confidencePct;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_result", columnDefinition = "jsonb")
    private Map<String, Object> rawResult;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Devuelve skinToneHex sin espacios de relleno.
     */
    public String getSkinToneHex() {
        return skinToneHex == null ? null : skinToneHex.trim();
    }
}