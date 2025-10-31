package com.soulware.platform.docexcelparser.domain.service;

import com.soulware.platform.docexcelparser.domain.model.PatientProfile;

/**
 * Interfaz de servicio de dominio para publicación de mensajes
 * Define el contrato sin depender de implementaciones específicas (ActiveMQ, RabbitMQ, etc.)
 */
public interface IMessagePublisherService {
    
    /**
     * Publica los datos de un paciente procesado
     * @param patientProfile Paciente procesado
     * @return true si se publicó exitosamente, false en caso contrario
     */
    boolean publishPatientData(PatientProfile patientProfile);
    
    /**
     * Cierra la conexión del servicio
     */
    void closeConnection();
}

