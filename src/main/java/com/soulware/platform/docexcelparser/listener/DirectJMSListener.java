package com.soulware.platform.docexcelparser.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import javax.jms.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.soulware.platform.docexcelparser.parser.ExcelPatientParser;
import com.soulware.platform.docexcelparser.entity.PatientProfile;
import com.soulware.platform.docexcelparser.service.PatientJSONSenderService;
import com.soulware.platform.docexcelparser.service.MinioService;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Direct JMS Listener basado en la implementación que funciona en lab62
 * Conecta directamente a ActiveMQ sin usar JNDI de WildFly
 */
@WebListener
public class DirectJMSListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(DirectJMSListener.class.getName());

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "excel-input-queue";

    private static Connection connection;
    private static Session session;
    private static MessageConsumer consumer;
    private static ObjectMapper objectMapper;

    private static final ConcurrentLinkedQueue<String> receivedMessages = new ConcurrentLinkedQueue<>();
    private static final List<String> messageHistory = Collections.synchronizedList(new ArrayList<>());
           private static final List<PatientProfile> processedPatients = Collections.synchronizedList(new ArrayList<>());
           private static volatile boolean isInitialized = false;
           private static String listenerStatus = "No inicializado";
           private static ExcelPatientParser excelParser;
           private static PatientJSONSenderService patientJSONSender;
           private static MinioService minioService;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        synchronized (DirectJMSListener.class) {
            if (isInitialized) {
                logger.info("DirectJMSListener already initialized, skipping...");
                return;
            }

            System.out.println("==========================================");
            System.out.println("=== DIRECT JMS LISTENER STARTING ===");
            System.out.println("=== BASADO EN LAB62 IMPLEMENTATION ===");
            System.out.println("=== BROKER: " + BROKER_URL + " ===");
            System.out.println("=== QUEUE: " + QUEUE_NAME + " ===");
            System.out.println("=== TIMESTAMP: " + LocalDateTime.now() + " ===");
            System.out.println("==========================================");
            logger.info("=== DirectJMSListener STARTING ===");

            try {
                closeExistingConnections(); // Asegurarse de cerrar conexiones anteriores

                // Inicializar ObjectMapper para JSON
                objectMapper = new ObjectMapper();
                
                       // Inicializar el parser de Excel
                       excelParser = new ExcelPatientParser();
                       
                       // Inicializar el servicio para enviar JSON a cola separada
                       patientJSONSender = new PatientJSONSenderService();
                       
                       // Inicializar el servicio MinIO
                       minioService = new MinioService();

                // Usar ActiveMQConnectionFactory directamente como en lab62
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
                connection = factory.createConnection();
                connection.setClientID("DocExcelParserDirectClient");

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue(QUEUE_NAME);

                consumer = session.createConsumer(queue);

                // Set message listener usando la misma lógica que lab62
                consumer.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(Message message) {
                        try {
                            System.out.println("==========================================");
                            System.out.println("=== MESSAGE RECEIVED BY DIRECT JMS LISTENER ===");
                            System.out.println("=== TIMESTAMP: " + LocalDateTime.now() + " ===");
                            System.out.println("==========================================");
                            logger.info("=== MESSAGE RECEIVED BY DIRECT JMS LISTENER ===");

                            if (message instanceof TextMessage textMessage) {
                                String messageText = textMessage.getText();
                                System.out.println("Message type: TextMessage");
                                System.out.println("Message length: " + messageText.length() + " characters");
                                System.out.println("Message content:");
                                System.out.println(messageText);
                                System.out.println("==========================================");
                                logger.info("Received TEXT message: " + messageText.length() + " characters");

                                // Procesar JSON y Excel
                                try {
                                    JsonNode jsonNode = objectMapper.readTree(messageText);
                                    
                                    // Extraer campos específicos
                                    String messageId = jsonNode.has("messageId") ? jsonNode.get("messageId").asText() : "unknown";
                                    String fileName = jsonNode.has("fileName") ? jsonNode.get("fileName").asText() : "unknown.xlsx";
                                    String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "unknown";
                                    
                                    System.out.println("Message ID: " + messageId);
                                    System.out.println("File Name: " + fileName);
                                    System.out.println("Status: " + status);
                                    
                                    // Procesar Excel desde MinIO si existe fileKey
                                    if (jsonNode.has("fileKey")) {
                                        String fileKey = jsonNode.get("fileKey").asText();
                                        String minioFileName = jsonNode.has("fileName") ? 
                                            jsonNode.get("fileName").asText() : "excel-file.xlsx";
                                        System.out.println("MinIO File Key: " + fileKey);
                                        System.out.println("File Name: " + minioFileName);
                                        
                                        // Procesar el Excel desde MinIO
                                        processExcelFromMinIO(fileKey, minioFileName, messageId);
                                    } else if (jsonNode.has("excelBase64")) {
                                        // Mantener compatibilidad con mensajes antiguos (Base64)
                                        String base64 = jsonNode.get("excelBase64").asText();
                                        System.out.println("Excel Base64 Length: " + base64.length() + " characters");
                                        
                                        // Procesar el Excel desde Base64 (legacy)
                                        processExcelData(base64, fileName, messageId);
                                    } else {
                                        System.out.println("No Excel data found in message (neither fileKey nor excelBase64)");
                                    }
                                    
                                } catch (Exception jsonException) {
                                    System.out.println("Error parsing JSON: " + jsonException.getMessage());
                                    System.out.println("Raw message: " + messageText);
                                }

                                // Generar ID único para el mensaje
                                String messageId = "direct-jms-" + System.currentTimeMillis();

                                // Almacenar mensaje para consulta
                                receivedMessages.offer(messageId);
                                receivedMessages.offer(messageText);
                                messageHistory.add(messageText);

                                // Mantener solo los últimos 10 mensajes
                                if (messageHistory.size() > 10) {
                                    messageHistory.remove(0);
                                }

                                System.out.println("==========================================");
                                System.out.println("=== MESSAGE STORED SUCCESSFULLY ===");
                                System.out.println("=== MESSAGE ID: " + messageId + " ===");
                                System.out.println("=== TOTAL MESSAGES: " + receivedMessages.size() + " ===");
                                System.out.println("==========================================");

                            } else {
                                System.out.println("=== MENSAJE JMS RECIBIDO (NO TEXT): " + message.getClass().getName() + " ===");
                                logger.info("JMS Message Received (Non-Text): " + message.getClass().getName());
                                receivedMessages.offer("Non-text message: " + message.getClass().getName());
                            }
                        } catch (JMSException e) {
                            logger.log(Level.SEVERE, "Error processing JMS message: " + e.getMessage(), e);
                        }
                    }
                });

                // Start connection
                logger.info("Starting connection...");
                System.out.println("Starting connection...");
                connection.start();

                isInitialized = true;
                listenerStatus = "DIRECT JMS ACTIVO - Escuchando en " + QUEUE_NAME + " (basado en lab62)";

                System.out.println("==========================================");
                System.out.println("=== SUCCESS: DIRECT JMS LISTENER CONNECTED ===");
                System.out.println("=== QUEUE: " + QUEUE_NAME + " ===");
                System.out.println("=== LISTENING FOR REAL MESSAGES... ===");
                System.out.println("=== BASADO EN IMPLEMENTACIÓN LAB62 ===");
                System.out.println("==========================================");
                logger.info("SUCCESS: DirectJMSListener connected to " + QUEUE_NAME);

            } catch (JMSException e) {
                System.err.println("==========================================");
                System.err.println("=== FAILED TO INITIALIZE DIRECT JMS LISTENER ===");
                System.err.println("=== ERROR: " + e.getMessage() + " ===");
                System.err.println("=== FALLBACK: Using SimpleTestListener ===");
                System.err.println("==========================================");
                logger.log(Level.SEVERE, "FAILED to initialize DirectJMSListener: " + e.getMessage(), e);
                listenerStatus = "DIRECT JMS FALLIDO - " + e.getMessage();
                isInitialized = false;
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
           System.out.println("=== DIRECT JMS LISTENER STOPPING ===");
           logger.info("=== DirectJMSListener STOPPING ===");
           closeExistingConnections();
           clearMessages();
           
           // Cerrar conexión del servicio JSON
           if (patientJSONSender != null) {
               patientJSONSender.closeConnection();
           }
           
           // Cerrar conexión del servicio MinIO
           if (minioService != null) {
               minioService.close();
           }
           
           isInitialized = false;
           listenerStatus = "Detenido";
    }

    private static void closeExistingConnections() {
        try {
            if (consumer != null) {
                consumer.close();
                consumer = null;
            }
            if (session != null) {
                session.close();
                session = null;
            }
            if (connection != null) {
                connection.close();
                connection = null;
            }
            System.out.println("Direct JMS connections closed successfully");
            logger.info("Direct JMS connections closed successfully");
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Error closing Direct JMS resources: " + e.getMessage(), e);
        }
    }

    public static String getLastMessage() {
        if (messageHistory.isEmpty()) {
            return null;
        }
        return messageHistory.get(messageHistory.size() - 1);
    }

    public static List<String> getAllMessages() {
        return new ArrayList<>(receivedMessages);
    }

    public static String getListenerStatus() {
        return listenerStatus;
    }

    public static void clearMessages() {
        receivedMessages.clear();
        messageHistory.clear();
        System.out.println("=== MENSAJES LIMPIADOS DEL DIRECT JMS LISTENER ===");
        logger.info("Messages cleared from DirectJMSListener.");
    }

    /**
     * Verifica si el listener está inicializado
     * @return true si está inicializado, false en caso contrario
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Limpia el último mensaje leído para permitir leer el siguiente
     */
    public static void clearLastMessage() {
        if (!messageHistory.isEmpty()) {
            messageHistory.remove(messageHistory.size() - 1);
            System.out.println("=== ÚLTIMO MENSAJE LIMPIADO DEL DIRECT JMS LISTENER ===");
            logger.info("Last message cleared from DirectJMSListener.");
        }
    }

    /**
     * Procesa los datos de Excel desde MinIO
     */
    private static void processExcelFromMinIO(String fileKey, String fileName, String messageId) {
        try {
            System.out.println("==========================================");
            System.out.println("=== PROCESANDO EXCEL DESDE MINIO ===");
            System.out.println("=== FILE KEY: " + fileKey + " ===");
            System.out.println("=== FILE NAME: " + fileName + " ===");
            System.out.println("=== MESSAGE ID: " + messageId + " ===");
            System.out.println("==========================================");
            
            // Verificar que el archivo existe en MinIO
            if (!minioService.fileExists(fileKey)) {
                System.err.println("❌ FILE NOT FOUND in MinIO: " + fileKey);
                logger.severe("File not found in MinIO: " + fileKey);
                return;
            }
            
            // Obtener información del archivo
            MinioService.FileInfo fileInfo = minioService.getFileInfo(fileKey);
            if (fileInfo != null) {
                System.out.println("📄 File Info:");
                System.out.println("   Size: " + fileInfo.getSize() + " bytes");
                System.out.println("   Content-Type: " + fileInfo.getContentType());
                System.out.println("   Last Modified: " + fileInfo.getLastModified());
            }
            
            // Descargar Excel desde MinIO
            byte[] excelBytes = minioService.downloadFile(fileKey);
            System.out.println("✅ Excel downloaded from MinIO: " + excelBytes.length + " bytes");
            
            // Convertir bytes a Base64 para el parser (mantener compatibilidad)
            String base64Data = Base64.getEncoder().encodeToString(excelBytes);
            
            // Procesar con el parser existente
            PatientProfile patient = excelParser.parsePatientFromExcel(base64Data);
            List<PatientProfile> patients = List.of(patient);
            
            System.out.println("==========================================");
            System.out.println("=== EXCEL PROCESADO EXITOSAMENTE ===");
            System.out.println("=== PACIENTES ENCONTRADOS: " + patients.size() + " ===");
            System.out.println("==========================================");
            
            // Almacenar pacientes procesados
            processedPatients.addAll(patients);
            
            // Mantener solo los últimos 50 pacientes
            if (processedPatients.size() > 50) {
                processedPatients.subList(0, processedPatients.size() - 50).clear();
            }
            
            // Mostrar información de pacientes procesados
            for (int i = 0; i < patients.size(); i++) {
                PatientProfile patientItem = patients.get(i);
                System.out.println("Paciente " + (i + 1) + ": " + patientItem.getFirstNames() + " " + patientItem.getPaternalSurname());
            }
            
            // Enviar datos del paciente a cola separada
            for (PatientProfile patientItem : patients) {
                boolean sent = patientJSONSender.sendPatientDataToQueue(patientItem);
                if (sent) {
                    System.out.println("✅ Datos del paciente enviados a cola separada: " + patientItem.getFirstNames() + " " + patientItem.getPaternalSurname());
                } else {
                    System.err.println("❌ Error enviando datos del paciente a cola separada: " + patientItem.getFirstNames() + " " + patientItem.getPaternalSurname());
                }
            }
            
            // Opcional: eliminar archivo temporal de MinIO
            try {
                minioService.deleteFile(fileKey);
                System.out.println("🗑️  Archivo temporal eliminado de MinIO: " + fileKey);
            } catch (Exception e) {
                System.out.println("⚠️  No se pudo eliminar archivo temporal: " + e.getMessage());
            }
            
        } catch (IOException e) {
            System.err.println("❌ Error downloading Excel from MinIO: " + e.getMessage());
            logger.log(Level.SEVERE, "Error downloading Excel from MinIO: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Error processing Excel from MinIO: " + e.getMessage());
            logger.log(Level.SEVERE, "Error processing Excel from MinIO: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa los datos de Excel desde Base64 (método legacy para compatibilidad)
     */
    private static void processExcelData(String base64Data, String fileName, String messageId) {
        try {
            System.out.println("==========================================");
            System.out.println("=== PROCESANDO EXCEL DESDE BASE64 ===");
            System.out.println("=== FILE: " + fileName + " ===");
            System.out.println("=== MESSAGE ID: " + messageId + " ===");
            System.out.println("==========================================");
            
            // Decodificar Base64
            byte[] excelBytes = Base64.getDecoder().decode(base64Data);
            System.out.println("Excel bytes decoded: " + excelBytes.length + " bytes");
            
            // Procesar con el parser
            PatientProfile patient = excelParser.parsePatientFromExcel(base64Data);
            List<PatientProfile> patients = List.of(patient);
            
            System.out.println("==========================================");
            System.out.println("=== EXCEL PROCESADO EXITOSAMENTE ===");
            System.out.println("=== PACIENTES ENCONTRADOS: " + patients.size() + " ===");
            System.out.println("==========================================");
            
                   // Almacenar pacientes procesados
                   processedPatients.addAll(patients);
                   
                   // Mantener solo los últimos 50 pacientes
                   if (processedPatients.size() > 50) {
                       processedPatients.subList(0, processedPatients.size() - 50).clear();
                   }
                   
                   // Log de cada paciente
                   for (int i = 0; i < patients.size(); i++) {
                       PatientProfile patientItem = patients.get(i);
                       System.out.println("Paciente " + (i + 1) + ": " + patientItem.getFirstNames() + " " + patientItem.getPaternalSurname());
                   }
                   
                   // Enviar datos del paciente a cola separada
                   for (PatientProfile patientItem : patients) {
                       boolean sent = patientJSONSender.sendPatientDataToQueue(patientItem);
                       if (sent) {
                           System.out.println("✅ Datos del paciente enviados a cola separada: " + patientItem.getFirstNames() + " " + patientItem.getPaternalSurname());
                       } else {
                           System.err.println("❌ Error enviando datos del paciente a cola separada: " + patientItem.getFirstNames() + " " + patientItem.getPaternalSurname());
                       }
                   }
            
        } catch (Exception e) {
            System.err.println("==========================================");
            System.err.println("=== ERROR PROCESANDO EXCEL ===");
            System.err.println("=== ERROR: " + e.getMessage() + " ===");
            System.err.println("==========================================");
            logger.log(Level.SEVERE, "Error processing Excel data: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los pacientes procesados
     */
    public static List<PatientProfile> getProcessedPatients() {
        return new ArrayList<>(processedPatients);
    }

    /**
     * Obtiene el último paciente procesado
     */
    public static PatientProfile getLastProcessedPatient() {
        if (processedPatients.isEmpty()) {
            return null;
        }
        return processedPatients.get(processedPatients.size() - 1);
    }

    /**
     * Limpia los pacientes procesados
     */
    public static void clearProcessedPatients() {
        processedPatients.clear();
        System.out.println("=== PACIENTES PROCESADOS LIMPIADOS ===");
        logger.info("Processed patients cleared.");
    }
}
