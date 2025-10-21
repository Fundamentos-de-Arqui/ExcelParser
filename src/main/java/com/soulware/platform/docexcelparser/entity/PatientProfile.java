package com.soulware.platform.docexcelparser.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad que representa el perfil de un paciente
 * Basada en el diagrama de base de datos PatientProfile
 */
@Entity
@Table(name = "patient_profile")
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_profile_id")
    private Long patientProfileId;

    @Column(name = "first_names")
    private String firstNames;

    @Column(name = "paternal_surname")
    private String paternalSurname;

    @Column(name = "maternal_surname")
    private String maternalSurname;

    @Column(name = "identity_document_number")
    private String identityDocumentNumber;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "birth_place")
    private String birthPlace;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "age_first_appointment")
    private Integer ageFirstAppointment;

    @Column(name = "age_current")
    private Integer ageCurrent;

    @Column(name = "gender")
    private String gender;

    @Column(name = "marital_status")
    private String maritalStatus;

    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "district")
    private String district;

    @Column(name = "province")
    private String province;

    @Column(name = "region")
    private String region;

    @Column(name = "country")
    private String country;

    @Column(name = "religion")
    private String religion;

    @Column(name = "education_level")
    private String educationLevel;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "current_educational_institution")
    private String currentEducationalInstitution;

    // Constructores
    public PatientProfile() {
    }

    public PatientProfile(String firstNames, String paternalSurname, String maternalSurname) {
        this.firstNames = firstNames;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
    }

    // Getters y Setters
    public Long getPatientProfileId() {
        return patientProfileId;
    }

    public void setPatientProfileId(Long patientProfileId) {
        this.patientProfileId = patientProfileId;
    }

    public String getFirstNames() {
        return firstNames;
    }

    public void setFirstNames(String firstNames) {
        this.firstNames = firstNames;
    }

    public String getPaternalSurname() {
        return paternalSurname;
    }

    public void setPaternalSurname(String paternalSurname) {
        this.paternalSurname = paternalSurname;
    }

    public String getMaternalSurname() {
        return maternalSurname;
    }

    public void setMaternalSurname(String maternalSurname) {
        this.maternalSurname = maternalSurname;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getAgeFirstAppointment() {
        return ageFirstAppointment;
    }

    public void setAgeFirstAppointment(Integer ageFirstAppointment) {
        this.ageFirstAppointment = ageFirstAppointment;
    }

    public Integer getAgeCurrent() {
        return ageCurrent;
    }

    public void setAgeCurrent(Integer ageCurrent) {
        this.ageCurrent = ageCurrent;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getCurrentEducationalInstitution() {
        return currentEducationalInstitution;
    }

    public void setCurrentEducationalInstitution(String currentEducationalInstitution) {
        this.currentEducationalInstitution = currentEducationalInstitution;
    }

    @Override
    public String toString() {
        return "PatientProfile{" +
                "patientProfileId=" + patientProfileId +
                ", firstNames='" + firstNames + '\'' +
                ", paternalSurname='" + paternalSurname + '\'' +
                ", maternalSurname='" + maternalSurname + '\'' +
                ", identityDocumentNumber='" + identityDocumentNumber + '\'' +
                ", documentType='" + documentType + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", birthPlace='" + birthPlace + '\'' +
                ", birthDate=" + birthDate +
                ", ageFirstAppointment=" + ageFirstAppointment +
                ", ageCurrent=" + ageCurrent +
                ", gender='" + gender + '\'' +
                ", maritalStatus='" + maritalStatus + '\'' +
                ", currentAddress='" + currentAddress + '\'' +
                ", district='" + district + '\'' +
                ", province='" + province + '\'' +
                ", region='" + region + '\'' +
                ", country='" + country + '\'' +
                ", religion='" + religion + '\'' +
                ", educationLevel='" + educationLevel + '\'' +
                ", occupation='" + occupation + '\'' +
                ", currentEducationalInstitution='" + currentEducationalInstitution + '\'' +
                '}';
    }
}

