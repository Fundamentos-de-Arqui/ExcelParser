package com.soulware.platform.docexcelparser.listener;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import jakarta.jms.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ActiveMQ listener usando ServletContextListener y MessageListener
 * Basado en el patrón del proyecto profiles que funciona correctamente
 */
@WebListener
public class ActiveMQMessageListener implements ServletContextListener, MessageListener {
    
    private static final Logger logger = Logger.getLogger(ActiveMQMessageListener.class.getName());
    
    // Static para evitar múltiples instancias
    private static Connection connection;
    private static Session session;
    private static MessageConsumer consumer;
    private static boolean isInitialized = false;
    
    // Almacenar mensajes leídos para consulta
    private static final Map<String, String> receivedMessages = new ConcurrentHashMap<>();
    private static final List<String> messageHistory = new ArrayList<>();
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Evitar inicialización múltiple
        synchronized (ActiveMQMessageListener.class) {
            if (isInitialized) {
                System.out.println("ActiveMQMessageListener already initialized, skipping...");
                logger.info("ActiveMQMessageListener already initialized, skipping...");
                return;
            }
            
            System.out.println("==========================================");
            System.out.println("=== ACTIVEMQ MESSAGE LISTENER STARTING ===");
            System.out.println("=== COLA: excel.input.queue ===");
            System.out.println("=== TIMESTAMP: " + java.time.LocalDateTime.now() + " ===");
            System.out.println("==========================================");
            logger.info("=== ActiveMQMessageListener STARTING ===");
            
            try {
                // Cerrar conexiones existentes si las hay
                closeExistingConnections();
                
                // Create connection factory
                logger.info("Creating ActiveMQ connection factory...");
                System.out.println("Creating ActiveMQ connection factory...");
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
                
                // Create connection
                logger.info("Creating connection...");
                System.out.println("Creating connection...");
                connection = connectionFactory.createConnection();
                
                // Create session
                logger.info("Creating session...");
                System.out.println("Creating session...");
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                
                // Create queue
                logger.info("Creating queue: excel.input.queue");
                System.out.println("Creating queue: excel.input.queue");
                Queue queue = session.createQueue("excel.input.queue");
                
                // Create consumer
                logger.info("Creating consumer...");
                System.out.println("Creating consumer...");
                consumer = session.createConsumer(queue);
                consumer.setMessageListener(this);
                
                // Start connection
                logger.info("Starting connection...");
                System.out.println("Starting connection...");
                connection.start();
                
                isInitialized = true;
                
                System.out.println("==========================================");
                System.out.println("=== SUCCESS: LISTENER CONNECTED ===");
                System.out.println("=== COLA: excel.input.queue ===");
                System.out.println("=== LISTENING FOR MESSAGES... ===");
                System.out.println("==========================================");
                logger.info("SUCCESS: ActiveMQMessageListener connected to excel.input.queue");
                
            } catch (Exception e) {
                System.err.println("==========================================");
                System.err.println("=== FAILED TO INITIALIZE LISTENER ===");
                System.err.println("=== ERROR: " + e.getMessage() + " ===");
                System.err.println("==========================================");
                logger.log(Level.SEVERE, "FAILED to initialize ActiveMQMessageListener: " + e.getMessage(), e);
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("==========================================");
        System.out.println("=== ACTIVEMQ MESSAGE LISTENER STOPPING ===");
        System.out.println("==========================================");
        logger.info("=== ActiveMQMessageListener STOPPING ===");
        
        closeExistingConnections();
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
            isInitialized = false;
            System.out.println("Connections closed successfully");
            logger.info("Connections closed successfully");
        } catch (Exception e) {
            System.err.println("Error closing connections: " + e.getMessage());
            logger.log(Level.WARNING, "Error closing connections: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onMessage(Message message) {
        try {
            System.out.println("==========================================");
            System.out.println("=== MESSAGE RECEIVED BY ACTIVEMQ LISTENER ===");
            System.out.println("=== TIMESTAMP: " + java.time.LocalDateTime.now() + " ===");
            System.out.println("==========================================");
            logger.info("=== MESSAGE RECEIVED BY ACTIVEMQ LISTENER ===");
            
            if (message instanceof TextMessage textMessage) {
                String messageText = textMessage.getText();
                System.out.println("Message type: TextMessage");
                System.out.println("Message length: " + messageText.length() + " characters");
                System.out.println("First 200 chars: " + messageText.substring(0, Math.min(200, messageText.length())));
                logger.info("Received TEXT message: " + messageText.length() + " characters");
                
                // Generar ID único para el mensaje
                String messageId = "jms-" + System.currentTimeMillis();
                
                // Almacenar mensaje para consulta
                receivedMessages.put(messageId, messageText);
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
                System.out.println("Received non-text message: " + message.getClass().getSimpleName());
                logger.warning("Received non-text message: " + message.getClass().getSimpleName());
            }
            
            System.out.println("=== MESSAGE PROCESSING COMPLETE ===");
            logger.info("=== MESSAGE PROCESSING COMPLETE ===");
            
        } catch (Exception e) {
            System.err.println("==========================================");
            System.err.println("=== ERROR PROCESSING MESSAGE ===");
            System.err.println("=== ERROR: " + e.getMessage() + " ===");
            System.err.println("==========================================");
            logger.log(Level.SEVERE, "ERROR processing message: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
    
    /**
     * Obtiene el último mensaje recibido
     * @return Contenido del último mensaje o null si no hay mensajes
     */
    public static String getLastMessage() {
        if (messageHistory.isEmpty()) {
            return null;
        }
        return messageHistory.get(messageHistory.size() - 1);
    }
    
    /**
     * Obtiene todos los mensajes almacenados
     * @return Lista de todos los mensajes recibidos
     */
    public static List<String> getAllMessages() {
        return new ArrayList<>(messageHistory);
    }
    
    /**
     * Obtiene un mensaje específico por ID
     * @param messageId ID del mensaje
     * @return Contenido del mensaje o null si no existe
     */
    public static String getMessageById(String messageId) {
        return receivedMessages.get(messageId);
    }
    
    /**
     * Obtiene información del estado del listener
     * @return String con información del estado
     */
    public static String getListenerStatus() {
        return "ActiveMQ JMS Listener activo - Mensajes recibidos: " + receivedMessages.size() +
               " - Historial: " + messageHistory.size() + " - Conexión: " + (isInitialized ? "ACTIVA" : "INACTIVA");
    }
    
    /**
     * Limpia todos los mensajes almacenados
     */
    public static void clearMessages() {
        receivedMessages.clear();
        messageHistory.clear();
        System.out.println("=== MENSAJES LIMPIADOS DEL ACTIVEMQ LISTENER ===");
    }
    
    /**
     * Verifica si el listener está inicializado
     * @return true si está inicializado, false en caso contrario
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}
