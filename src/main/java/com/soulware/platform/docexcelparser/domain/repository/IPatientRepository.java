package com.soulware.platform.docexcelparser.domain.repository;

import com.soulware.platform.docexcelparser.domain.model.PatientProfile;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para PatientProfile en la capa de dominio
 * Define los contratos para persistencia sin depender de implementaciones específicas
 */
public interface IPatientRepository {
    
    /**
     * Guarda un paciente
     * @param patientProfile Paciente a guardar
     * @return Paciente guardado
     */
    PatientProfile save(PatientProfile patientProfile);
    
    /**
     * Busca un paciente por ID
     * @param id ID del paciente
     * @return Optional con el paciente si existe
     */
    Optional<PatientProfile> findById(Long id);
    
    /**
     * Obtiene todos los pacientes
     * @return Lista de pacientes
     */
    List<PatientProfile> findAll();
    
    /**
     * Elimina un paciente
     * @param patientProfile Paciente a eliminar
     */
    void delete(PatientProfile patientProfile);
}

