package com.soulware.platform.docexcelparser.listener;

import com.soulware.platform.docexcelparser.config.JMSConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebListener de Jakarta Servlet para polling automático de la cola
 * Este listener se inicializa automáticamente cuando se despliega la aplicación
 */
@WebListener
public class QueuePollingListener implements ServletContextListener {
    
    private JMSConfig jmsConfig;
    private ScheduledExecutorService scheduler;
    private volatile boolean isRunning = false;
    
    // Almacenar mensajes leídos para consulta
    private static final Map<String, String> receivedMessages = new ConcurrentHashMap<>();
    private static final List<String> messageHistory = new ArrayList<>();
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("==========================================");
        System.out.println("=== WEBLISTENER INICIALIZADO ===");
        System.out.println("=== MODO: POLLING AUTOMÁTICO CADA 5 SEGUNDOS ===");
        System.out.println("=== COLA: excel.input.queue ===");
        System.out.println("=== TIMESTAMP: " + java.time.LocalDateTime.now() + " ===");
        System.out.println("==========================================");
        
        // Inicializar JMSConfig
        jmsConfig = new JMSConfig();
        System.out.println("JMSConfig inicializado en WebListener");
        
        // Iniciar polling automático
        startPolling();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("==========================================");
        System.out.println("=== WEBLISTENER DESTRUIDO ===");
        System.out.println("=== TIMESTAMP: " + java.time.LocalDateTime.now() + " ===");
        System.out.println("==========================================");
        
        stopPolling();
    }
    
    /**
     * Inicia el polling automático de la cola
     */
    private void startPolling() {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                pollQueue();
            } catch (Exception e) {
                System.err.println("Error en polling automático: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
        
        System.out.println("Polling automático iniciado - verificando cola cada 5 segundos");
    }
    
    /**
     * Detiene el polling automático
     */
    private void stopPolling() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Polling automático detenido");
    }
    
    /**
     * Hace polling de la cola para obtener nuevos mensajes
     */
    private void pollQueue() {
        try {
            System.out.println("==========================================");
            System.out.println("=== POLLING AUTOMÁTICO DE COLA ===");
            System.out.println("=== TIMESTAMP: " + java.time.LocalDateTime.now() + " ===");
            System.out.println("==========================================");
            
            // Usar el método básico de lectura (más rápido)
            System.out.println("Iniciando lectura básica de cola...");
            String message = jmsConfig.readBasicMessageFromQueue();
            System.out.println("Lectura básica completada. Resultado: " + (message != null ? message.length() + " caracteres" : "null"));
            
            if (message != null && !message.trim().isEmpty()) {
                System.out.println("==========================================");
                System.out.println("=== MENSAJE ENCONTRADO EN POLLING AUTOMÁTICO ===");
                System.out.println("=== LONGITUD: " + message.length() + " caracteres ===");
                System.out.println("=== PRIMEROS 100 CHARS: " + message.substring(0, Math.min(100, message.length())) + " ===");
                System.out.println("==========================================");
                
                // Generar ID único para el mensaje
                String messageId = "auto-poll-" + System.currentTimeMillis();
                
                // Almacenar mensaje para consulta
                receivedMessages.put(messageId, message);
                messageHistory.add(message);
                
                // Mantener solo los últimos 10 mensajes
                if (messageHistory.size() > 10) {
                    messageHistory.remove(0);
                }
                
                System.out.println("Mensaje almacenado con ID: " + messageId);
                System.out.println("Total mensajes almacenados: " + receivedMessages.size());
            } else {
                System.out.println("=== NO HAY MENSAJES EN LA COLA (POLLING AUTOMÁTICO) ===");
            }
            
        } catch (Exception e) {
            System.err.println("==========================================");
            System.err.println("=== ERROR EN POLLING AUTOMÁTICO ===");
            System.err.println("=== ERROR: " + e.getMessage() + " ===");
            System.err.println("=== TIPO DE ERROR: " + e.getClass().getSimpleName() + " ===");
            System.err.println("==========================================");
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
        return "WebListener activo - Mensajes recibidos: " + receivedMessages.size() + 
               " - Historial: " + messageHistory.size() + " - Polling automático cada 5 segundos";
    }
    
    /**
     * Limpia todos los mensajes almacenados
     */
    public static void clearMessages() {
        receivedMessages.clear();
        messageHistory.clear();
        System.out.println("=== MENSAJES LIMPIADOS DEL WEBLISTENER ===");
    }
    
    /**
     * Fuerza un polling inmediato
     */
    public void forcePoll() {
        System.out.println("=== POLLING FORZADO DESDE WEBLISTENER ===");
        pollQueue();
    }
}
