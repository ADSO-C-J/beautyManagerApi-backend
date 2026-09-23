package com.beautyManager.beautyManagerApi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Clave primaria compuesta de la tabla puente staff_services {staff_id, service_id}.
 * Se usa con @EmbeddedId para respetar el esquema PostgreSQL (no existe columna id).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffServiceId implements Serializable {

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaffServiceId)) return false;
        StaffServiceId that = (StaffServiceId) o;
        return Objects.equals(staffId, that.staffId) && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffId, serviceId);
    }
}