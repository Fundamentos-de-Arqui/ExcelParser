package com.soulware.platform.docexcelparser.service;

import com.soulware.platform.docexcelparser.listener.QueuePollingListener;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Servicio para obtener mensajes del WebListener de Jakarta Servlet
 * Este servicio actúa como interfaz entre el servlet y el QueuePollingListener
 */
@ApplicationScoped
public class WebListenerService {
    
    /**
     * Obtiene el último mensaje recibido por el WebListener
     * @return Contenido del último mensaje o null si no hay mensajes
     */
    public String getLastMessage() {
        try {
            System.out.println("=== OBTENIENDO ÚLTIMO MENSAJE DEL WEBLISTENER ===");
            String message = QueuePollingListener.getLastMessage();
            
            if (message != null) {
                System.out.println("Mensaje obtenido: " + message.length() + " caracteres");
                return message;
            } else {
                System.out.println("No hay mensajes disponibles en el WebListener");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensaje del WebListener: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene todos los mensajes almacenados en el WebListener
     * @return Lista de todos los mensajes recibidos
     */
    public List<String> getAllMessages() {
        try {
            System.out.println("=== OBTENIENDO TODOS LOS MENSAJES DEL WEBLISTENER ===");
            List<String> messages = QueuePollingListener.getAllMessages();
            System.out.println("Total mensajes obtenidos: " + messages.size());
            return messages;
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensajes del WebListener: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Obtiene el estado del WebListener
     * @return String con información del estado
     */
    public String getListenerStatus() {
        try {
            return QueuePollingListener.getListenerStatus();
        } catch (Exception e) {
            return "Error obteniendo estado del WebListener: " + e.getMessage();
        }
    }
    
    /**
     * Limpia todos los mensajes almacenados en el WebListener
     */
    public void clearMessages() {
        try {
            System.out.println("=== LIMPIANDO MENSAJES DEL WEBLISTENER ===");
            QueuePollingListener.clearMessages();
        } catch (Exception e) {
            System.err.println("Error limpiando mensajes del WebListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica si hay mensajes disponibles en el WebListener
     * @return true si hay mensajes disponibles, false en caso contrario
     */
    public boolean hasMessages() {
        try {
            String lastMessage = QueuePollingListener.getLastMessage();
            return lastMessage != null && !lastMessage.trim().isEmpty();
        } catch (Exception e) {
            System.err.println("Error verificando mensajes del WebListener: " + e.getMessage());
            return false;
        }
    }
}