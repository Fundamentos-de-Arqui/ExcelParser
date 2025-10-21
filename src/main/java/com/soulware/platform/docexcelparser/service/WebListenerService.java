package com.soulware.platform.docexcelparser.service;

import com.soulware.platform.docexcelparser.listener.SimpleTestListener;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Servicio para obtener mensajes del SimpleTestListener (Testing sin JMS)
 * Este servicio actúa como interfaz entre el servlet y el SimpleTestListener
 */
@ApplicationScoped
public class WebListenerService {
    
    /**
     * Obtiene el último mensaje recibido por el SimpleTestListener
     * @return Contenido del último mensaje o null si no hay mensajes
     */
    public String getLastMessage() {
        try {
            System.out.println("=== OBTENIENDO ÚLTIMO MENSAJE DEL TEST LISTENER ===");
            String message = SimpleTestListener.getLastMessage();
            
            if (message != null) {
                System.out.println("Mensaje obtenido: " + message.length() + " caracteres");
                return message;
            } else {
                System.out.println("No hay mensajes disponibles en el listener");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensaje del SimpleTestListener: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene todos los mensajes almacenados en el SimpleTestListener
     * @return Lista de todos los mensajes recibidos
     */
    public List<String> getAllMessages() {
        try {
            System.out.println("=== OBTENIENDO TODOS LOS MENSAJES DEL TEST LISTENER ===");
            List<String> messages = SimpleTestListener.getAllMessages();
            System.out.println("Total mensajes obtenidos: " + messages.size());
            return messages;
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensajes del SimpleTestListener: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Obtiene el estado del SimpleTestListener
     * @return String con información del estado
     */
    public String getListenerStatus() {
        try {
            return SimpleTestListener.getListenerStatus();
        } catch (Exception e) {
            return "Error obteniendo estado del listener: " + e.getMessage();
        }
    }
    
    /**
     * Limpia todos los mensajes almacenados en el SimpleTestListener
     */
    public void clearMessages() {
        try {
            System.out.println("=== LIMPIANDO MENSAJES DEL TEST LISTENER ===");
            SimpleTestListener.clearMessages();
        } catch (Exception e) {
            System.err.println("Error limpiando mensajes del SimpleTestListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica si hay mensajes disponibles en el SimpleTestListener
     * @return true si hay mensajes disponibles, false en caso contrario
     */
    public boolean hasMessages() {
        try {
            String lastMessage = SimpleTestListener.getLastMessage();
            return lastMessage != null && !lastMessage.trim().isEmpty();
        } catch (Exception e) {
            System.err.println("Error verificando mensajes del SimpleTestListener: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el listener está inicializado
     * @return true si está inicializado, false en caso contrario
     */
    public boolean isListenerInitialized() {
        try {
            return SimpleTestListener.isInitialized();
        } catch (Exception e) {
            System.err.println("Error verificando estado del listener: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el listener JMS real está inicializado (siempre false por ahora)
     * @return false (JMS no configurado)
     */
    public boolean isRealJMSInitialized() {
        return false; // JMS no configurado en WildFly
    }
}