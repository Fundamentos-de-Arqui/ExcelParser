package com.soulware.platform.docexcelparser.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un Responsable Legal o Acompañante
 * Relación One-to-Many con PatientProfile
 */
@Entity
@Table(name = "legal_guardian")
public class LegalGuardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "legal_guardian_id")
    private Long legalGuardianId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id")
    private PatientProfile patientProfile;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "identity_document_number")
    private String identityDocumentNumber;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructores
    public LegalGuardian() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LegalGuardian(String fullName, String identityDocumentNumber, String relationship) {
        this();
        this.fullName = fullName;
        this.identityDocumentNumber = identityDocumentNumber;
        this.relationship = relationship;
    }

    // Getters y Setters
    public Long getLegalGuardianId() {
        return legalGuardianId;
    }

    public void setLegalGuardianId(Long legalGuardianId) {
        this.legalGuardianId = legalGuardianId;
    }

    public PatientProfile getPatientProfile() {
        return patientProfile;
    }

    public void setPatientProfile(PatientProfile patientProfile) {
        this.patientProfile = patientProfile;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getIdentityDocumentNumber() {
        return identityDocumentNumber;
    }

    public void setIdentityDocumentNumber(String identityDocumentNumber) {
        this.identityDocumentNumber = identityDocumentNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "LegalGuardian{" +
                "legalGuardianId=" + legalGuardianId +
                ", fullName='" + fullName + '\'' +
                ", identityDocumentNumber='" + identityDocumentNumber + '\'' +
                ", documentType='" + documentType + '\'' +
                ", relationship='" + relationship + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

