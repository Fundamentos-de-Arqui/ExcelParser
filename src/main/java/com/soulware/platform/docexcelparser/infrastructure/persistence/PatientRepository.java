package com.soulware.platform.docexcelparser.infrastructure.persistence;

import com.soulware.platform.docexcelparser.domain.model.PatientProfile;
import com.soulware.platform.docexcelparser.domain.repository.IPatientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación JPA del repositorio de pacientes
 */
@ApplicationScoped
public class PatientRepository implements IPatientRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Override
    @Transactional
    public PatientProfile save(PatientProfile patientProfile) {
        if (patientProfile.getPatientProfileId() == null) {
            entityManager.persist(patientProfile);
            return patientProfile;
        } else {
            return entityManager.merge(patientProfile);
        }
    }

    @Override
    public Optional<PatientProfile> findById(Long id) {
        PatientProfile patient = entityManager.find(PatientProfile.class, id);
        return Optional.ofNullable(patient);
    }

    @Override
    public List<PatientProfile> findAll() {
        return entityManager.createQuery("SELECT p FROM PatientProfile p", PatientProfile.class)
                .getResultList();
    }

    @Override
    @Transactional
    public void delete(PatientProfile patientProfile) {
        PatientProfile managed = entityManager.find(PatientProfile.class, patientProfile.getPatientProfileId());
        if (managed != null) {
            entityManager.remove(managed);
        }
    }
}

