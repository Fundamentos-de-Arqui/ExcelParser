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

/**
 * Listener simple para verificar que el sistema funciona
 * Sin JMS por ahora - solo para testing
 */
@WebListener
public class SimpleTestListener implements ServletContextListener {
    
    private static final Logger logger = Logger.getLogger(SimpleTestListener.class.getName());
    
    // Almacenar mensajes de prueba
    private static final Map<String, String> receivedMessages = new ConcurrentHashMap<>();
    private static final List<String> messageHistory = new ArrayList<>();
    private static boolean isInitialized = false;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        synchronized (SimpleTestListener.class) {
            if (isInitialized) {
                System.out.println("SimpleTestListener already initialized, skipping...");
                return;
            }
            
            System.out.println("==========================================");
            System.out.println("=== SIMPLE TEST LISTENER STARTING ===");
            System.out.println("=== MODO: TESTING SIN JMS ===");
            System.out.println("=== TIMESTAMP: " + java.time.LocalDateTime.now() + " ===");
            System.out.println("==========================================");
            logger.info("=== SimpleTestListener STARTING ===");
            
            // Simular un mensaje de prueba
            String testMessage = "{\"messageId\":\"test-001\",\"fileName\":\"test.xlsx\",\"status\":\"TEST\",\"receivedAt\":\"" + 
                                java.time.LocalDateTime.now() + "\",\"excelBase64\":\"VGVzdCBNZXNzYWdl\"}";
            
            receivedMessages.put("test-001", testMessage);
            messageHistory.add(testMessage);
            
            isInitialized = true;
            
            System.out.println("==========================================");
            System.out.println("=== SUCCESS: TEST LISTENER INITIALIZED ===");
            System.out.println("=== MESSAGE SIMULADO CREADO ===");
            System.out.println("==========================================");
            logger.info("SUCCESS: SimpleTestListener initialized with test message");
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("==========================================");
        System.out.println("=== SIMPLE TEST LISTENER STOPPING ===");
        System.out.println("==========================================");
        logger.info("=== SimpleTestListener STOPPING ===");
        
        receivedMessages.clear();
        messageHistory.clear();
        isInitialized = false;
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
        return "Simple Test Listener activo - Mensajes simulados: " + receivedMessages.size() +
               " - Historial: " + messageHistory.size() + " - Estado: " + (isInitialized ? "ACTIVO" : "INACTIVO");
    }
    
    /**
     * Limpia todos los mensajes almacenados
     */
    public static void clearMessages() {
        receivedMessages.clear();
        messageHistory.clear();
        System.out.println("=== MENSAJES LIMPIADOS DEL TEST LISTENER ===");
    }
    
    /**
     * Verifica si el listener está inicializado
     * @return true si está inicializado, false en caso contrario
     */
    public static boolean isInitialized() {
        return isInitialized;
    }
}
