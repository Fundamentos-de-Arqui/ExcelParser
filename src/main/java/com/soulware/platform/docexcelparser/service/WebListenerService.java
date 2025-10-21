package com.soulware.platform.docexcelparser.service;

import com.soulware.platform.docexcelparser.listener.QueueMessageListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Servicio para obtener mensajes del QueueMessageListener (Polling)
 * Este servicio actúa como interfaz entre el servlet y el QueueMessageListener
 */
@ApplicationScoped
public class WebListenerService {
    
    @Inject
    private QueueMessageListener queueMessageListener;
    
    /**
     * Obtiene el último mensaje recibido por el QueueMessageListener
     * @return Contenido del último mensaje o null si no hay mensajes
     */
    public String getLastMessage() {
        try {
            System.out.println("=== OBTENIENDO ÚLTIMO MENSAJE DEL POLLING LISTENER ===");
            String message = QueueMessageListener.getLastMessage();
            
            if (message != null) {
                System.out.println("Mensaje obtenido: " + message.length() + " caracteres");
                return message;
            } else {
                System.out.println("No hay mensajes disponibles en el listener");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensaje del QueueMessageListener: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene todos los mensajes almacenados en el QueueMessageListener
     * @return Lista de todos los mensajes recibidos
     */
    public List<String> getAllMessages() {
        try {
            System.out.println("=== OBTENIENDO TODOS LOS MENSAJES DEL POLLING LISTENER ===");
            List<String> messages = QueueMessageListener.getAllMessages();
            System.out.println("Total mensajes obtenidos: " + messages.size());
            return messages;
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensajes del QueueMessageListener: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Obtiene el estado del QueueMessageListener
     * @return String con información del estado
     */
    public String getListenerStatus() {
        try {
            return QueueMessageListener.getListenerStatus();
        } catch (Exception e) {
            return "Error obteniendo estado del listener: " + e.getMessage();
        }
    }
    
    /**
     * Limpia todos los mensajes almacenados en el QueueMessageListener
     */
    public void clearMessages() {
        try {
            System.out.println("=== LIMPIANDO MENSAJES DEL POLLING LISTENER ===");
            QueueMessageListener.clearMessages();
        } catch (Exception e) {
            System.err.println("Error limpiando mensajes del QueueMessageListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica si hay mensajes disponibles en el QueueMessageListener
     * @return true si hay mensajes disponibles, false en caso contrario
     */
    public boolean hasMessages() {
        try {
            String lastMessage = QueueMessageListener.getLastMessage();
            return lastMessage != null && !lastMessage.trim().isEmpty();
        } catch (Exception e) {
            System.err.println("Error verificando mensajes del QueueMessageListener: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Fuerza un polling inmediato de la cola
     */
    public void forcePoll() {
        try {
            System.out.println("=== FORZANDO POLLING INMEDIATO ===");
            queueMessageListener.forcePoll();
        } catch (Exception e) {
            System.err.println("Error forzando polling: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
