package com.soulware.platform.docexcelparser.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.soulware.platform.docexcelparser.domain.model.PatientProfile;
import com.soulware.platform.docexcelparser.domain.model.LegalGuardian;
import com.soulware.platform.docexcelparser.domain.model.ReferredTherapist;
import com.soulware.platform.docexcelparser.domain.service.IMessagePublisherService;
import com.soulware.platform.docexcelparser.infrastructure.config.ApplicationConfig;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Servicio para enviar datos completos del paciente a una cola separada
 * Incluye paciente, médico tratante y responsables legales en formato JSON
 */
@ApplicationScoped
public class PatientJSONSenderService implements IMessagePublisherService {

    private static final Logger logger = Logger.getLogger(PatientJSONSenderService.class.getName());
    
    private final ApplicationConfig config = new ApplicationConfig();
    
    private ObjectMapper objectMapper;
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public PatientJSONSenderService() {
        initializeObjectMapper();
    }

    /**
     * Inicializa el ObjectMapper con soporte para Java Time
     */
    private void initializeObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Envía los datos completos del paciente a la cola separada
     * @param patientProfile Perfil del paciente con médico tratante y responsables legales
     * @return true si se envió exitosamente, false en caso contrario
     */
    public boolean sendPatientDataToQueue(PatientProfile patientProfile) {
        try {
            System.out.println("==========================================");
            System.out.println("=== ENVIANDO DATOS DE PACIENTE A COLA SEPARADA ===");
            String queueName = config.getJmsQueuePatientData();
            System.out.println("=== COLA: " + queueName + " ===");
            System.out.println("=== TIMESTAMP: " + LocalDateTime.now() + " ===");
            System.out.println("==========================================");

            // Crear conexión JMS si no existe
            if (connection == null) {
                initializeJMSConnection();
            }

            // Crear estructura JSON del paciente
            Map<String, Object> patientData = createPatientDataJSON(patientProfile);

            // Convertir a JSON
            String jsonData = objectMapper.writeValueAsString(patientData);
            System.out.println("JSON generado:");
            System.out.println(jsonData);
            System.out.println("Longitud del JSON: " + jsonData.length() + " caracteres");

            // Crear mensaje JMS
            TextMessage message = session.createTextMessage(jsonData);
            
            // Agregar propiedades del mensaje
            message.setStringProperty("messageType", "PATIENT_DATA");
            message.setStringProperty("patientId", patientProfile.getPatientProfileId() != null ? 
                patientProfile.getPatientProfileId().toString() : "unknown");
            message.setStringProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            message.setStringProperty("source", "DocExcelParser");

            // Enviar mensaje
            producer.send(message);
            
            System.out.println("==========================================");
            System.out.println("✅ MENSAJE ENVIADO EXITOSAMENTE");
            System.out.println("✅ COLA: " + queueName);
            System.out.println("✅ PACIENTE: " + patientProfile.getFirstNames() + " " + patientProfile.getPaternalSurname());
            System.out.println("✅ MÉDICO: " + (patientProfile.getTherapistName() != null ? patientProfile.getTherapistName() : "No especificado"));
            System.out.println("✅ RESPONSABLES: " + patientProfile.getLegalGuardians().size());
            System.out.println("==========================================");

            return true;

        } catch (Exception e) {
            System.err.println("==========================================");
            System.err.println("❌ ERROR ENVIANDO DATOS DE PACIENTE");
            System.err.println("❌ ERROR: " + e.getMessage());
            System.err.println("==========================================");
            logger.severe("Error sending patient data to queue: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inicializa la conexión JMS
     */
    private void initializeJMSConnection() throws JMSException {
        System.out.println("=== INICIALIZANDO CONEXIÓN JMS PARA COLA SEPARADA ===");
        
        String brokerUrl = config.getJmsBrokerUrl();
        String queueName = config.getJmsQueuePatientData();
        
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        connection.setClientID("DocExcelParserPatientSender");
        
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName);
        
        producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        
        connection.start();
        
        System.out.println("✅ Conexión JMS inicializada para cola: " + queueName);
    }

    /**
     * Crea la estructura JSON completa del paciente
     * Estructura: { "patient": { datos_paciente + referredTherapist + legalGuardians }, "metadata": {...} }
     */
    private Map<String, Object> createPatientDataJSON(PatientProfile patient) {
        Map<String, Object> patientData = new HashMap<>();
        
        // Datos básicos del paciente
        Map<String, Object> patientInfo = new HashMap<>();
        patientInfo.put("patientId", patient.getPatientProfileId());
        patientInfo.put("firstNames", patient.getFirstNames());
        patientInfo.put("paternalSurname", patient.getPaternalSurname());
        patientInfo.put("maternalSurname", patient.getMaternalSurname());
        patientInfo.put("identityDocumentNumber", patient.getIdentityDocumentNumber());
        patientInfo.put("phone", patient.getPhone());
        patientInfo.put("email", patient.getEmail());
        patientInfo.put("birthPlace", patient.getBirthPlace());
        patientInfo.put("birthDate", patient.getBirthDate());
        patientInfo.put("ageCurrent", patient.getAgeCurrent());
        patientInfo.put("gender", patient.getGender());
        patientInfo.put("maritalStatus", patient.getMaritalStatus());
        patientInfo.put("currentAddress", patient.getCurrentAddress());
        patientInfo.put("district", patient.getDistrict());
        patientInfo.put("province", patient.getProvince());
        patientInfo.put("region", patient.getRegion());
        patientInfo.put("country", patient.getCountry());
        patientInfo.put("religion", patient.getReligion());
        patientInfo.put("educationLevel", patient.getEducationLevel());
        patientInfo.put("occupation", patient.getOccupation());
        patientInfo.put("currentEducationalInstitution", patient.getCurrentEducationalInstitution());
        
        // Médico tratante directamente como string dentro del objeto patient
        ReferredTherapist therapist = patient.getReferredTherapist();
        if (therapist != null && therapist.hasName()) {
            patientInfo.put("referredTherapist", therapist.getTherapistName());
        } else {
            patientInfo.put("referredTherapist", null);
        }
        
        // Responsables legales directamente como array dentro del objeto patient
        List<Map<String, Object>> legalGuardians = new java.util.ArrayList<>();
        for (LegalGuardian guardian : patient.getLegalGuardians()) {
            Map<String, Object> guardianInfo = new HashMap<>();
            guardianInfo.put("fullName", guardian.getFullName());
            guardianInfo.put("identityDocumentNumber", guardian.getIdentityDocumentNumber());
            guardianInfo.put("documentType", guardian.getDocumentType());
            guardianInfo.put("relationship", guardian.getRelationship());
            guardianInfo.put("phoneNumber", guardian.getPhoneNumber());
            guardianInfo.put("email", guardian.getEmail());
            guardianInfo.put("createdAt", guardian.getCreatedAt());
            legalGuardians.add(guardianInfo);
        }
        patientInfo.put("legalGuardians", legalGuardians);
        
        // El objeto patient ahora contiene todo
        patientData.put("patient", patientInfo);
        
        // Metadatos del mensaje (fuera del objeto patient)
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("processedAt", LocalDateTime.now());
        metadata.put("source", "DocExcelParser");
        metadata.put("version", "1.0");
        metadata.put("totalLegalGuardians", legalGuardians.size());
        metadata.put("hasTherapist", therapist != null && therapist.hasName());
        
        patientData.put("metadata", metadata);
        
        return patientData;
    }

    @Override
    public boolean publishPatientData(PatientProfile patientProfile) {
        return sendPatientDataToQueue(patientProfile);
    }

    /**
     * Cierra la conexión JMS
     */
    public void closeConnection() {
        try {
            if (producer != null) {
                producer.close();
                producer = null;
            }
            if (session != null) {
                session.close();
                session = null;
            }
            if (connection != null) {
                connection.close();
                connection = null;
            }
            System.out.println("✅ Conexión JMS cerrada para PatientJSONSenderService");
        } catch (JMSException e) {
            logger.severe("Error closing JMS connection: " + e.getMessage());
        }
    }
}
