package com.soulware.platform.docexcelparser.service;

import com.soulware.platform.docexcelparser.listener.ActiveMQMessageListener;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Servicio para obtener mensajes del ActiveMQMessageListener (JMS nativo)
 * Este servicio actúa como interfaz entre el servlet y el ActiveMQMessageListener
 */
@ApplicationScoped
public class WebListenerService {
    
    /**
     * Obtiene el último mensaje recibido por el ActiveMQMessageListener
     * @return Contenido del último mensaje o null si no hay mensajes
     */
    public String getLastMessage() {
        try {
            System.out.println("=== OBTENIENDO ÚLTIMO MENSAJE DEL ACTIVEMQ LISTENER ===");
            String message = ActiveMQMessageListener.getLastMessage();
            
            if (message != null) {
                System.out.println("Mensaje obtenido: " + message.length() + " caracteres");
                return message;
            } else {
                System.out.println("No hay mensajes disponibles en el listener");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensaje del ActiveMQMessageListener: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene todos los mensajes almacenados en el ActiveMQMessageListener
     * @return Lista de todos los mensajes recibidos
     */
    public List<String> getAllMessages() {
        try {
            System.out.println("=== OBTENIENDO TODOS LOS MENSAJES DEL ACTIVEMQ LISTENER ===");
            List<String> messages = ActiveMQMessageListener.getAllMessages();
            System.out.println("Total mensajes obtenidos: " + messages.size());
            return messages;
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensajes del ActiveMQMessageListener: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Obtiene el estado del ActiveMQMessageListener
     * @return String con información del estado
     */
    public String getListenerStatus() {
        try {
            return ActiveMQMessageListener.getListenerStatus();
        } catch (Exception e) {
            return "Error obteniendo estado del listener: " + e.getMessage();
        }
    }
    
    /**
     * Limpia todos los mensajes almacenados en el ActiveMQMessageListener
     */
    public void clearMessages() {
        try {
            System.out.println("=== LIMPIANDO MENSAJES DEL ACTIVEMQ LISTENER ===");
            ActiveMQMessageListener.clearMessages();
        } catch (Exception e) {
            System.err.println("Error limpiando mensajes del ActiveMQMessageListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica si hay mensajes disponibles en el ActiveMQMessageListener
     * @return true si hay mensajes disponibles, false en caso contrario
     */
    public boolean hasMessages() {
        try {
            String lastMessage = ActiveMQMessageListener.getLastMessage();
            return lastMessage != null && !lastMessage.trim().isEmpty();
        } catch (Exception e) {
            System.err.println("Error verificando mensajes del ActiveMQMessageListener: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el listener está inicializado
     * @return true si está inicializado, false en caso contrario
     */
    public boolean isListenerInitialized() {
        try {
            return ActiveMQMessageListener.isInitialized();
        } catch (Exception e) {
            System.err.println("Error verificando estado del listener: " + e.getMessage());
            return false;
        }
    }
}