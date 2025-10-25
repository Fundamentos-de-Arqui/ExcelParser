package com.soulware.platform.docexcelparser.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import javax.jms.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.soulware.platform.docexcelparser.service.PatientFormToExcelService;
import com.soulware.platform.docexcelparser.service.MinioService;
import com.soulware.platform.docexcelparser.service.APIGatewayPublisherService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener para procesar formularios de pacientes desde JSON y convertirlos a Excel
 * Cola: excelParser_patientForm
 */
@WebListener
public class PatientFormListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(PatientFormListener.class.getName());

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "excelParser_patientForm";

    private static Connection connection;
    private static Session session;
    private static MessageConsumer consumer;
    private static ObjectMapper objectMapper;

    private static final ConcurrentLinkedQueue<String> receivedMessages = new ConcurrentLinkedQueue<>();
    private static final List<String> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private static volatile boolean isInitialized = false;
    private static String listenerStatus = "No inicializado";
    
    // Servicios
    private static PatientFormToExcelService excelService;
    private static MinioService minioService;
    private static APIGatewayPublisherService apiGatewayPublisher;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("==========================================");
        System.out.println("=== PATIENT FORM LISTENER STARTING ===");
        System.out.println("=== COLA: " + QUEUE_NAME + " ===");
        System.out.println("=== TIMESTAMP: " + LocalDateTime.now() + " ===");
        System.out.println("==========================================");
        
        try {
            // Inicializar servicios
            excelService = new PatientFormToExcelService();
            minioService = new MinioService();
            apiGatewayPublisher = new APIGatewayPublisherService();
            
            // Inicializar ObjectMapper
            objectMapper = new ObjectMapper();
            
            // Configurar conexión JMS
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // Crear cola
            Queue queue = session.createQueue(QUEUE_NAME);
            
            // Crear consumer
            consumer = session.createConsumer(queue);
            
            // Configurar listener de mensajes
            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String messageText = textMessage.getText();
                        
                        System.out.println("==========================================");
                        System.out.println("=== PATIENT FORM MESSAGE RECEIVED ===");
                        System.out.println("=== TIMESTAMP: " + LocalDateTime.now() + " ===");
                        System.out.println("==========================================");
                        System.out.println("Message content:");
                        System.out.println(messageText);
                        System.out.println("==========================================");
                        
                        // Procesar el JSON del formulario de paciente
                        processPatientFormJSON(messageText, message.getJMSMessageID());
                        
                        // Almacenar mensaje para consulta
                        receivedMessages.offer(message.getJMSMessageID());
                        receivedMessages.offer(messageText);
                        messageHistory.add(messageText);
                        
                        // Mantener solo los últimos 10 mensajes
                        if (messageHistory.size() > 10) {
                            messageHistory.remove(0);
                        }
                        
                        System.out.println("==========================================");
                        System.out.println("=== MESSAGE STORED SUCCESSFULLY ===");
                        System.out.println("=== MESSAGE ID: " + message.getJMSMessageID() + " ===");
                        System.out.println("=== TOTAL MESSAGES: " + receivedMessages.size() + " ===");
                        System.out.println("==========================================");
                        
                    } else if (message instanceof BytesMessage) {
                        BytesMessage bytesMessage = (BytesMessage) message;
                        byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
                        bytesMessage.readBytes(messageBytes);
                        String messageText = new String(messageBytes);
                        
                        System.out.println("==========================================");
                        System.out.println("=== PATIENT FORM BYTES MESSAGE RECEIVED ===");
                        System.out.println("=== TIMESTAMP: " + LocalDateTime.now() + " ===");
                        System.out.println("==========================================");
                        System.out.println("Message content:");
                        System.out.println(messageText);
                        System.out.println("==========================================");
                        
                        // Procesar el JSON del formulario de paciente
                        processPatientFormJSON(messageText, message.getJMSMessageID());
                        
                        // Almacenar mensaje para consulta
                        receivedMessages.offer(message.getJMSMessageID());
                        receivedMessages.offer(messageText);
                        messageHistory.add(messageText);
                        
                        // Mantener solo los últimos 10 mensajes
                        if (messageHistory.size() > 10) {
                            messageHistory.remove(0);
                        }
                        
                    } else {
                        System.out.println("=== MENSAJE NO SOPORTADO: " + message.getClass().getName() + " ===");
                        logger.info("Unsupported message type: " + message.getClass().getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing patient form message: " + e.getMessage());
                    logger.log(Level.SEVERE, "Error processing patient form message: " + e.getMessage(), e);
                }
            });

            // Iniciar conexión
            connection.start();
            
            isInitialized = true;
            listenerStatus = "PATIENT FORM LISTENER ACTIVO - Escuchando en " + QUEUE_NAME;
            
            System.out.println("==========================================");
            System.out.println("=== SUCCESS: PATIENT FORM LISTENER CONNECTED ===");
            System.out.println("=== QUEUE: " + QUEUE_NAME + " ===");
            System.out.println("=== LISTENING FOR PATIENT FORM MESSAGES... ===");
            System.out.println("==========================================");
            logger.info("SUCCESS: PatientFormListener connected to " + QUEUE_NAME);

        } catch (JMSException e) {
            System.err.println("==========================================");
            System.err.println("=== FAILED TO INITIALIZE PATIENT FORM LISTENER ===");
            System.err.println("=== ERROR: " + e.getMessage() + " ===");
            System.err.println("==========================================");
            logger.log(Level.SEVERE, "FAILED to initialize PatientFormListener: " + e.getMessage(), e);
            listenerStatus = "PATIENT FORM LISTENER FALLIDO - " + e.getMessage();
            isInitialized = false;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== PATIENT FORM LISTENER STOPPING ===");
        logger.info("=== PatientFormListener STOPPING ===");
        closeConnections();
        clearMessages();
    }

    /**
     * Procesa el JSON del formulario de paciente y lo convierte a Excel
     */
    private static void processPatientFormJSON(String jsonMessage, String messageId) {
        try {
            System.out.println("==========================================");
            System.out.println("=== PROCESANDO FORMULARIO DE PACIENTE ===");
            System.out.println("=== MESSAGE ID: " + messageId + " ===");
            System.out.println("==========================================");
            
            // Parsear JSON
            JsonNode jsonNode = objectMapper.readTree(jsonMessage);
            
            // Validar que tenga los campos básicos
            if (!jsonNode.has("firstNames") || !jsonNode.has("paternalSurname")) {
                System.err.println("❌ JSON inválido: faltan campos obligatorios");
                logger.severe("Invalid JSON: missing required fields");
                return;
            }
            
            // Convertir JSON a Excel
            byte[] excelBytes = excelService.convertPatientFormToExcel(jsonNode);
            System.out.println("✅ Excel generado: " + excelBytes.length + " bytes");
            
            // Generar nombre de archivo único
            String fileName = generateFileName(jsonNode);
            String fileKey = "generated/" + fileName;
            
            // Subir Excel a Supabase Storage
            boolean uploadSuccess = minioService.uploadFile(fileKey, excelBytes, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            
            if (uploadSuccess) {
                System.out.println("✅ Excel subido a Supabase Storage: " + fileKey);
                
                // Generar URL de descarga
                String downloadUrl = minioService.generatePresignedGetUrl(fileKey, 60); // 60 minutos
                
                if (downloadUrl != null) {
                    System.out.println("✅ URL de descarga generada: " + downloadUrl);
                    
                    // Enviar URL al API Gateway
                    boolean apiSuccess = apiGatewayPublisher.sendExcelLinkToAPIGateway(downloadUrl, fileName, messageId);
                    
                    if (apiSuccess) {
                        System.out.println("✅ URL enviada al API Gateway exitosamente");
                        System.out.println("==========================================");
                        System.out.println("=== PROCESAMIENTO COMPLETADO EXITOSAMENTE ===");
                        System.out.println("=== ARCHIVO: " + fileName + " ===");
                        System.out.println("=== URL: " + downloadUrl + " ===");
                        System.out.println("==========================================");
                    } else {
                        System.err.println("❌ Error enviando URL al API Gateway");
                        logger.severe("Failed to send URL to API Gateway");
                    }
                } else {
                    System.err.println("❌ Error generando URL de descarga");
                    logger.severe("Failed to generate download URL");
                }
            } else {
                System.err.println("❌ Error subiendo Excel a Supabase Storage");
                logger.severe("Failed to upload Excel to Supabase Storage");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error procesando formulario de paciente: " + e.getMessage());
            logger.log(Level.SEVERE, "Error processing patient form: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un nombre de archivo único basado en los datos del paciente
     */
    private static String generateFileName(JsonNode jsonNode) {
        try {
            String firstName = jsonNode.has("firstNames") ? jsonNode.get("firstNames").asText() : "Paciente";
            String paternalSurname = jsonNode.has("paternalSurname") ? jsonNode.get("paternalSurname").asText() : "SinApellido";
            String timestamp = String.valueOf(System.currentTimeMillis());
            
            // Limpiar caracteres especiales
            firstName = firstName.replaceAll("[^a-zA-Z0-9]", "");
            paternalSurname = paternalSurname.replaceAll("[^a-zA-Z0-9]", "");
            
            return firstName + "_" + paternalSurname + "_" + timestamp + ".xlsx";
        } catch (Exception e) {
            return "paciente_" + System.currentTimeMillis() + ".xlsx";
        }
    }

    /**
     * Cierra las conexiones JMS
     */
    private static void closeConnections() {
        try {
            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
            System.out.println("Patient Form JMS connections closed successfully");
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Error closing Patient Form JMS connections: " + e.getMessage(), e);
        }
    }

    /**
     * Limpia los mensajes almacenados
     */
    public static void clearMessages() {
        receivedMessages.clear();
        messageHistory.clear();
        System.out.println("Patient Form messages cleared");
    }

    // Métodos estáticos para consulta
    public static boolean isInitialized() {
        return isInitialized;
    }

    public static String getListenerStatus() {
        return listenerStatus;
    }

    public static String getLastMessage() {
        return receivedMessages.isEmpty() ? null : receivedMessages.peek();
    }

    public static List<String> getAllMessages() {
        return new ArrayList<>(messageHistory);
    }
}
