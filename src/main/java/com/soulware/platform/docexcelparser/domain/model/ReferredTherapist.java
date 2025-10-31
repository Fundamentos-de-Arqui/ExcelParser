package com.soulware.platform.docexcelparser.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.Objects;

/**
 * Value Object de dominio que representa el nombre del médico tratante principal
 * Almacena únicamente el nombre del profesional médico
 */
@Embeddable
public class ReferredTherapist {

    @Column(name = "referred_therapist_name")
    private String therapistName;

    // Constructor por defecto
    public ReferredTherapist() {
    }

    // Constructor con nombre
    public ReferredTherapist(String therapistName) {
        this.therapistName = therapistName;
    }

    // Getters y Setters
    public String getTherapistName() {
        return therapistName;
    }

    public void setTherapistName(String therapistName) {
        this.therapistName = therapistName;
    }

    // Métodos de Value Object
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferredTherapist that = (ReferredTherapist) o;
        return Objects.equals(therapistName, that.therapistName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(therapistName);
    }

    @Override
    public String toString() {
        return "ReferredTherapist{" +
                "therapistName='" + therapistName + '\'' +
                '}';
    }

    // Método de conveniencia para verificar si tiene nombre
    public boolean hasName() {
        return therapistName != null && !therapistName.trim().isEmpty();
    }

    // Método de conveniencia para obtener nombre o valor por defecto
    public String getNameOrDefault(String defaultValue) {
        return hasName() ? therapistName : defaultValue;
    }
}

