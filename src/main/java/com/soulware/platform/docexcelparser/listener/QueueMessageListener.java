package com.soulware.platform.docexcelparser.listener;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.soulware.platform.docexcelparser.config.JMSConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Servicio de polling manual para leer mensajes de la cola JMS
 * Este servicio hace polling periódico de la cola en lugar de usar @MessageDriven
 */
@ApplicationScoped
public class QueueMessageListener {
    
    @Inject
    private JMSConfig jmsConfig;
    
    // Almacenar mensajes leídos para consulta
    private static final Map<String, String> receivedMessages = new ConcurrentHashMap<>();
    private static final List<String> messageHistory = new ArrayList<>();
    
    private ScheduledExecutorService scheduler;
    private volatile boolean isRunning = false;
    
    @PostConstruct
    public void init() {
        System.out.println("=== QUEUE MESSAGE LISTENER INICIALIZADO (POLLING) ===");
        System.out.println("Cola: excel.input.queue");
        System.out.println("Modo: Polling manual cada 5 segundos");
        System.out.println("==========================================");
        
        // Iniciar polling cada 5 segundos
        startPolling();
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("=== QUEUE MESSAGE LISTENER DESTRUIDO ===");
        stopPolling();
    }
    
    /**
     * Inicia el polling de la cola
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
                System.err.println("Error en polling de cola: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
        
        System.out.println("Polling iniciado - verificando cola cada 5 segundos");
    }
    
    /**
     * Detiene el polling de la cola
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
        
        System.out.println("Polling detenido");
    }
    
    /**
     * Hace polling de la cola para obtener nuevos mensajes
     */
    private void pollQueue() {
        try {
            System.out.println("=== POLLING COLA ===");
            
            // Usar el método básico de lectura
            String message = jmsConfig.readBasicMessageFromQueue();
            
            if (message != null && !message.trim().isEmpty()) {
                System.out.println("Mensaje encontrado en polling: " + message.length() + " caracteres");
                
                // Generar ID único para el mensaje
                String messageId = "poll-" + System.currentTimeMillis();
                
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
                System.out.println("No hay mensajes en la cola");
            }
            
        } catch (Exception e) {
            System.err.println("Error en polling de cola: " + e.getMessage());
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
        return "Listener activo (Polling) - Mensajes recibidos: " + receivedMessages.size() + 
               " - Historial: " + messageHistory.size() + " - Polling cada 5 segundos";
    }
    
    /**
     * Limpia todos los mensajes almacenados
     */
    public static void clearMessages() {
        receivedMessages.clear();
        messageHistory.clear();
        System.out.println("=== MENSAJES LIMPIADOS DEL LISTENER ===");
    }
    
    /**
     * Fuerza un polling inmediato
     */
    public void forcePoll() {
        System.out.println("=== POLLING FORZADO ===");
        pollQueue();
    }
}
