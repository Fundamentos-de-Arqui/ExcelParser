package com.soulware.platform.docexcelparser.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Listener JMS real usando Jakarta JMS APIs estándar
 * Se conecta a ActiveMQ usando JNDI lookup
 */
@WebListener
public class RealJMSListener implements ServletContextListener {
    
    private static final Logger logger = Logger.getLogger(RealJMSListener.class.getName());
    
    // Static para evitar múltiples instancias
    private static jakarta.jms.Connection connection;
    private static jakarta.jms.Session session;
    private static jakarta.jms.MessageConsumer consumer;
    private static boolean isInitialized = false;
    
    // Almacenar mensajes leídos para consulta
    private static final Map<String, String> receivedMessages = new ConcurrentHashMap<>();
    private static final List<String> messageHistory = new ArrayList<>();
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        synchronized (RealJMSListener.class) {
            if (isInitialized) {
                System.out.println("RealJMSListener already initialized, skipping...");
                logger.info("RealJMSListener already initialized, skipping...");
                return;
            }
            
            System.out.println("==========================================");
            System.out.println("=== REAL JMS LISTENER STARTING ===");
            System.out.println("=== COLA: excel.input.queue ===");
            System.out.println("=== TIMESTAMP: " + java.time.LocalDateTime.now() + " ===");
            System.out.println("==========================================");
            logger.info("=== RealJMSListener STARTING ===");
            
            try {
                // Cerrar conexiones existentes si las hay
                closeExistingConnections();
                
                // Intentar usar JNDI lookup para obtener ConnectionFactory
                logger.info("Looking up ConnectionFactory via JNDI...");
                System.out.println("Looking up ConnectionFactory via JNDI...");
                
                Context context = new InitialContext();
                
                // Intentar diferentes nombres JNDI comunes
                jakarta.jms.ConnectionFactory connectionFactory = null;
                String[] jndiNames = {
                    "java:/ConnectionFactory",
                    "java:/JmsXA", 
                    "java:jboss/DefaultJMSConnectionFactory",
                    "java:comp/DefaultJMSConnectionFactory"
                };
                
                for (String jndiName : jndiNames) {
                    try {
                        System.out.println("Trying JNDI name: " + jndiName);
                        connectionFactory = (jakarta.jms.ConnectionFactory) context.lookup(jndiName);
                        System.out.println("SUCCESS: Found ConnectionFactory at " + jndiName);
                        break;
                    } catch (NamingException e) {
                        System.out.println("FAILED: " + jndiName + " - " + e.getMessage());
                    }
                }
                
                if (connectionFactory == null) {
                    throw new RuntimeException("No ConnectionFactory found in JNDI. JMS might not be configured in WildFly.");
                }
                
                // Create connection
                logger.info("Creating connection...");
                System.out.println("Creating connection...");
                connection = connectionFactory.createConnection();
                
                // Create session
                logger.info("Creating session...");
                System.out.println("Creating session...");
                session = connection.createSession(false, jakarta.jms.Session.AUTO_ACKNOWLEDGE);
                
                // Create queue
                logger.info("Creating queue: excel.input.queue");
                System.out.println("Creating queue: excel.input.queue");
                jakarta.jms.Queue queue = session.createQueue("excel.input.queue");
                
                // Create consumer
                logger.info("Creating consumer...");
                System.out.println("Creating consumer...");
                consumer = session.createConsumer(queue);
                
                // Set message listener
                consumer.setMessageListener(new jakarta.jms.MessageListener() {
                    @Override
                    public void onMessage(jakarta.jms.Message message) {
                        try {
                            System.out.println("==========================================");
                            System.out.println("=== MESSAGE RECEIVED BY REAL JMS LISTENER ===");
                            System.out.println("=== TIMESTAMP: " + java.time.LocalDateTime.now() + " ===");
                            System.out.println("==========================================");
                            logger.info("=== MESSAGE RECEIVED BY REAL JMS LISTENER ===");
                            
                            if (message instanceof jakarta.jms.TextMessage textMessage) {
                                String messageText = textMessage.getText();
                                System.out.println("Message type: TextMessage");
                                System.out.println("Message length: " + messageText.length() + " characters");
                                System.out.println("First 200 chars: " + messageText.substring(0, Math.min(200, messageText.length())));
                                logger.info("Received TEXT message: " + messageText.length() + " characters");
                                
                                // Generar ID único para el mensaje
                                String messageId = "jms-real-" + System.currentTimeMillis();
                                
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
                });
                
                // Start connection
                logger.info("Starting connection...");
                System.out.println("Starting connection...");
                connection.start();
                
                isInitialized = true;
                
                System.out.println("==========================================");
                System.out.println("=== SUCCESS: REAL JMS LISTENER CONNECTED ===");
                System.out.println("=== COLA: excel.input.queue ===");
                System.out.println("=== LISTENING FOR REAL MESSAGES... ===");
                System.out.println("==========================================");
                logger.info("SUCCESS: RealJMSListener connected to excel.input.queue");
                
            } catch (Exception e) {
                System.err.println("==========================================");
                System.err.println("=== FAILED TO INITIALIZE REAL JMS LISTENER ===");
                System.err.println("=== ERROR: " + e.getMessage() + " ===");
                System.err.println("=== FALLBACK: Using SimpleTestListener ===");
                System.err.println("==========================================");
                logger.log(Level.SEVERE, "FAILED to initialize RealJMSListener: " + e.getMessage(), e);
                e.printStackTrace();
                
                // No lanzar excepción para que el sistema siga funcionando con SimpleTestListener
                isInitialized = false;
            }
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("==========================================");
        System.out.println("=== REAL JMS LISTENER STOPPING ===");
        System.out.println("==========================================");
        logger.info("=== RealJMSListener STOPPING ===");
        
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
            System.out.println("JMS connections closed successfully");
            logger.info("JMS connections closed successfully");
        } catch (Exception e) {
            System.err.println("Error closing JMS connections: " + e.getMessage());
            logger.log(Level.WARNING, "Error closing JMS connections: " + e.getMessage(), e);
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
        return "Real JMS Listener - Estado: " + (isInitialized ? "ACTIVO" : "INACTIVO") +
               " - Mensajes recibidos: " + receivedMessages.size() +
               " - Historial: " + messageHistory.size();
    }
    
    /**
     * Limpia todos los mensajes almacenados
     */
    public static void clearMessages() {
        receivedMessages.clear();
        messageHistory.clear();
        System.out.println("=== MENSAJES LIMPIADOS DEL REAL JMS LISTENER ===");
    }
    
    /**
     * Verifica si el listener está inicializado
     * @return true si está inicializado, false en caso contrario
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}
