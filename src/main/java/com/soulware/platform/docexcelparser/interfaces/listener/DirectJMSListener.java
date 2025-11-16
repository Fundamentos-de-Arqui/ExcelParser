package com.soulware.platform.docexcelparser.interfaces.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import javax.jms.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.soulware.platform.docexcelparser.infrastructure.parser.ExcelPatientParser;
import com.soulware.platform.docexcelparser.domain.model.PatientProfile;
import com.soulware.platform.docexcelparser.infrastructure.messaging.PatientJSONSenderService;
import com.soulware.platform.docexcelparser.infrastructure.storage.MinioService;
import com.soulware.platform.docexcelparser.domain.service.IFileStorageService;
import com.soulware.platform.docexcelparser.infrastructure.config.ApplicationConfig;
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
    
    private static ApplicationConfig config = new ApplicationConfig();

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

            String brokerUrl = config.getJmsBrokerUrl();
            String queueName = config.getJmsQueueExcelInput();
            
            System.out.println("==========================================");
            System.out.println("=== DIRECT JMS LISTENER STARTING ===");
            System.out.println("=== BASADO EN LAB62 IMPLEMENTATION ===");
            System.out.println("=== BROKER: " + brokerUrl + " ===");
            System.out.println("=== QUEUE: " + queueName + " ===");
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
                // Configuración mejorada para Azure/redes remotas
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
                
                // Configuraciones importantes para Azure/ActiveMQ remoto:
                // - KeepAlive: mantiene la conexión viva (configurado por defecto)
                // - Timeouts: aumentados para redes con latencia
                // - Prefetch: CRÍTICO - reducir a 1 para evitar que mensajes queden bloqueados
                //   Con prefetch=1, solo se reserva 1 mensaje a la vez
                factory.setAlwaysSessionAsync(false);
                
                // Configurar prefetch policy para evitar mensajes bloqueados
                // Esto es CRÍTICO para que el polling manual funcione
                try {
                    // Intentar configurar prefetch a 1 (solo 1 mensaje a la vez)
                    // Esto evita que ActiveMQ reserve múltiples mensajes
                    String brokerUrlWithPrefetch = brokerUrl + "?jms.prefetchPolicy.queuePrefetch=1";
                    factory = new ActiveMQConnectionFactory(brokerUrlWithPrefetch);
                    factory.setAlwaysSessionAsync(false);
                    System.out.println("✅ Prefetch configurado a 1 (solo 1 mensaje a la vez)");
                } catch (Exception e) {
                    System.out.println("⚠️  No se pudo configurar prefetch, usando configuración por defecto");
                    logger.warning("Could not configure prefetch: " + e.getMessage());
                }
                
                connection = factory.createConnection();
                connection.setClientID("DocExcelParserDirectClient-" + System.currentTimeMillis());
                
                // Configurar exception listener para detectar problemas de conexión
                connection.setExceptionListener(new ExceptionListener() {
                    @Override
                    public void onException(JMSException exception) {
                        System.err.println("==========================================");
                        System.err.println("=== JMS CONNECTION EXCEPTION ===");
                        System.err.println("=== ERROR: " + exception.getMessage() + " ===");
                        System.err.println("=== IMPORTANTE: Problema de conexión detectado ===");
                        System.err.println("==========================================");
                        logger.severe("JMS Connection Exception: " + exception.getMessage());
                        exception.printStackTrace();
                    }
                });

                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue(queueName);

                // NO crear consumer aquí - se creará en el polling manual
                // Esto evita problemas con mensajes bloqueados
                System.out.println("⚠️  Consumer se creará en el polling manual");
                System.out.println("   (Esto evita problemas con mensajes bloqueados)");
                logger.info("Consumer will be created in manual polling thread");

                // Start connection
                logger.info("Starting connection...");
                System.out.println("Starting connection...");
                connection.start();
                
                // Iniciar polling manual (método principal para Azure/redes remotas)
                startManualPolling(session, queue);

                isInitialized = true;
                listenerStatus = "DIRECT JMS ACTIVO - Escuchando en " + queueName + " (basado en lab62)";

                System.out.println("==========================================");
                System.out.println("=== SUCCESS: DIRECT JMS LISTENER CONNECTED ===");
                System.out.println("=== QUEUE: " + queueName + " ===");
                System.out.println("=== LISTENING FOR REAL MESSAGES... ===");
                System.out.println("=== BASADO EN IMPLEMENTACIÓN LAB62 ===");
                System.out.println("==========================================");
                logger.info("SUCCESS: DirectJMSListener connected to " + queueName);

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
            // El consumer ahora se maneja en el polling thread, no aquí
            if (consumer != null) {
                try {
                    consumer.close();
                } catch (Exception e) {
                    // Ignorar si ya está cerrado
                }
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
            IFileStorageService.FileInfo fileInfo = minioService.getFileInfo(fileKey);
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
    
    /**
     * Inicia polling manual como respaldo al MessageListener
     * IMPORTANTE: Esto es especialmente útil para ActiveMQ en Azure/redes remotas
     * donde el MessageListener puede no activarse correctamente debido a:
     * - Latencia de red
     * - Firewalls/NAT
     * - Timeouts de conexión
     * - Problemas con keep-alive
     */
    private static void startManualPolling(Session session, Queue queue) {
        java.util.concurrent.Executors.newSingleThreadExecutor().submit(() -> {
            MessageConsumer pollingConsumer = null;
            try {
                logger.info("Starting manual polling thread (primary method for Azure/remote ActiveMQ)...");
                System.out.println("==========================================");
                System.out.println("=== INICIANDO POLLING MANUAL ===");
                System.out.println("=== MÉTODO PRINCIPAL PARA AZURE/ACTIVEMQ REMOTO ===");
                System.out.println("=== COLA: " + queue.getQueueName() + " ===");
                System.out.println("=== INTERVALO: 3 segundos ===");
                System.out.println("==========================================");
                
                // Crear consumer para polling (sin MessageListener)
                pollingConsumer = session.createConsumer(queue);
                System.out.println("✅ Consumer creado para polling manual");
                System.out.println();
                
                int pollCount = 0;
                int consecutiveErrors = 0;
                while (isInitialized && connection != null && session != null) {
                    try {
                        pollCount++;
                        
                        // Intentar recibir mensaje
                        Message message = null;
                        try {
                            // Primero intentar receiveNoWait() para no bloquear
                            message = pollingConsumer.receiveNoWait();
                            
                            // Si no hay mensaje, intentar con timeout
                            if (message == null) {
                                message = pollingConsumer.receive(2000); // 2 segundos timeout
                            }
                        } catch (JMSException receiveEx) {
                            System.err.println("Error en receive (Poll #" + pollCount + "): " + receiveEx.getMessage());
                            consecutiveErrors++;
                            
                            // Si hay muchos errores consecutivos, recrear el consumer
                            if (consecutiveErrors >= 3) {
                                System.out.println("⚠️  Muchos errores consecutivos, recreando consumer...");
                                try {
                                    if (pollingConsumer != null) {
                                        pollingConsumer.close();
                                    }
                                } catch (Exception e) {
                                    // Ignorar
                                }
                                
                                pollingConsumer = session.createConsumer(queue);
                                System.out.println("✅ Consumer recreado");
                                consecutiveErrors = 0;
                                Thread.sleep(1000);
                                continue;
                            }
                            
                            Thread.sleep(5000);
                            continue;
                        }
                        
                        // Resetear contador de errores si recibimos mensaje o timeout normal
                        consecutiveErrors = 0;
                        
                        if (message != null) {
                            System.out.println("==========================================");
                            System.out.println("=== MENSAJE RECIBIDO POR POLLING MANUAL ===");
                            System.out.println("=== POLL #" + pollCount + " ===");
                            System.out.println("=== TIMESTAMP: " + LocalDateTime.now() + " ===");
                            System.out.println("==========================================");
                            logger.info("=== MESSAGE RECEIVED BY MANUAL POLLING (Poll #" + pollCount + ") ===");
                            
                            // Procesar el mensaje
                            processMessage(message);
                            
                        } else if (pollCount % 20 == 0) {
                            // Log cada 20 polls (cada ~60 segundos) para confirmar que está funcionando
                            System.out.println("Polling activo (Poll #" + pollCount + ") - No hay mensajes en la cola");
                            logger.fine("Manual polling active (Poll #" + pollCount + ") - No messages");
                        }
                        
                        // Pequeña pausa antes del siguiente polling
                        Thread.sleep(3000); // 3 segundos entre polls
                        
                    } catch (JMSException e) {
                        logger.log(Level.WARNING, "Error in manual polling (Poll #" + pollCount + "): " + e.getMessage(), e);
                        System.err.println("Error en polling manual (Poll #" + pollCount + "): " + e.getMessage());
                        
                        // Verificar si la conexión sigue activa
                        if (connection != null) {
                            try {
                                // Intentar verificar el estado de la conexión
                                String clientId = connection.getClientID();
                                System.out.println("Verificando conexión - ClientID: " + clientId);
                            } catch (JMSException connEx) {
                                System.err.println("⚠️  CONEXIÓN PERDIDA - Intentando reconectar...");
                                logger.severe("Connection lost during polling: " + connEx.getMessage());
                                // Aquí podrías implementar lógica de reconexión si es necesario
                            }
                        }
                        
                        try {
                            Thread.sleep(5000); // Esperar 5 segundos antes de reintentar
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } catch (InterruptedException e) {
                        logger.info("Manual polling thread interrupted");
                        System.out.println("=== POLLING MANUAL INTERRUMPIDO ===");
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                System.out.println("=== POLLING MANUAL DETENIDO (Total polls: " + pollCount + ") ===");
                logger.info("Manual polling thread stopped (Total polls: " + pollCount + ")");
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error in manual polling thread: " + e.getMessage(), e);
                System.err.println("Error inesperado en polling manual: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Cerrar el consumer de polling
                try {
                    if (pollingConsumer != null) {
                        pollingConsumer.close();
                        System.out.println("✅ Consumer de polling cerrado");
                    }
                } catch (JMSException e) {
                    logger.log(Level.WARNING, "Error closing polling consumer: " + e.getMessage(), e);
                }
            }
        });
    }
    
    /**
     * Procesa un mensaje JMS (extraído del MessageListener para reutilización)
     * Este método es usado tanto por el MessageListener como por el polling manual
     */
    private static void processMessage(Message message) {
        try {
            String messageText = null;
            String messageId = null;
            
            if (message instanceof TextMessage textMessage) {
                messageText = textMessage.getText();
                messageId = message.getJMSMessageID();
                System.out.println("Message type: TextMessage");
                System.out.println("Message ID: " + messageId);
                System.out.println("Message length: " + messageText.length() + " characters");
                System.out.println("Message content:");
                System.out.println(messageText);
                System.out.println("==========================================");
                logger.info("Received TEXT message: " + messageText.length() + " characters");
                
            } else if (message instanceof BytesMessage bytesMessage) {
                byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(messageBytes);
                messageText = new String(messageBytes, "UTF-8");
                messageId = message.getJMSMessageID();
                
                System.out.println("==========================================");
                System.out.println("=== MENSAJE JMS RECIBIDO (BYTES) ===");
                System.out.println("=== ID: " + messageId + " ===");
                System.out.println("Message content:");
                System.out.println(messageText);
                System.out.println("==========================================");
                logger.info("Received BYTES message: " + messageText.length() + " characters");
            } else {
                System.out.println("=== MENSAJE JMS RECIBIDO (NO TEXT): " + message.getClass().getName() + " ===");
                logger.info("JMS Message Received (Non-Text): " + message.getClass().getName());
                return;
            }
            
            if (messageText == null || messageText.trim().isEmpty()) {
                System.out.println("Mensaje vacío, ignorando...");
                return;
            }
            
            // Procesar JSON y Excel
            try {
                JsonNode jsonNode = objectMapper.readTree(messageText);
                
                // Extraer campos específicos
                String jsonMessageId = jsonNode.has("messageId") ? jsonNode.get("messageId").asText() : "unknown";
                String fileName = jsonNode.has("fileName") ? jsonNode.get("fileName").asText() : "unknown.xlsx";
                String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "unknown";
                
                System.out.println("JSON Message ID: " + jsonMessageId);
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
                    processExcelFromMinIO(fileKey, minioFileName, jsonMessageId);
                } else if (jsonNode.has("excelBase64")) {
                    // Mantener compatibilidad con mensajes antiguos (Base64)
                    String base64 = jsonNode.get("excelBase64").asText();
                    System.out.println("Excel Base64 Length: " + base64.length() + " characters");
                    
                    // Procesar el Excel desde Base64 (legacy)
                    processExcelData(base64, fileName, jsonMessageId);
                } else {
                    System.out.println("No Excel data found in message (neither fileKey nor excelBase64)");
                }
                
            } catch (Exception jsonException) {
                System.out.println("Error parsing JSON: " + jsonException.getMessage());
                System.out.println("Raw message: " + messageText);
                logger.log(Level.WARNING, "Error parsing JSON: " + jsonException.getMessage(), jsonException);
            }
            
            // Generar ID único para el mensaje si no existe
            String storedMessageId = messageId != null ? messageId : "direct-jms-" + System.currentTimeMillis();
            
            // Almacenar mensaje para consulta
            receivedMessages.offer(storedMessageId);
            receivedMessages.offer(messageText);
            messageHistory.add(messageText);
            
            // Mantener solo los últimos 10 mensajes
            if (messageHistory.size() > 10) {
                messageHistory.remove(0);
            }
            
            System.out.println("==========================================");
            System.out.println("=== MESSAGE STORED SUCCESSFULLY ===");
            System.out.println("=== MESSAGE ID: " + storedMessageId + " ===");
            System.out.println("=== TOTAL MESSAGES: " + receivedMessages.size() + " ===");
            System.out.println("==========================================");
            
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Error processing JMS message: " + e.getMessage(), e);
            System.err.println("Error procesando mensaje JMS: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error processing message: " + e.getMessage(), e);
            System.err.println("Error inesperado procesando mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
