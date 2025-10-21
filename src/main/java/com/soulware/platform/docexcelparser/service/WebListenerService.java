package com.soulware.platform.docexcelparser.service;

import com.soulware.platform.docexcelparser.listener.RealJMSListener;
import com.soulware.platform.docexcelparser.listener.SimpleTestListener;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Servicio para obtener mensajes del listener JMS real o fallback al test listener
 * Este servicio actúa como interfaz entre el servlet y los listeners
 */
@ApplicationScoped
public class WebListenerService {
    
    /**
     * Obtiene el último mensaje recibido por el listener activo
     * @return Contenido del último mensaje o null si no hay mensajes
     */
    public String getLastMessage() {
        try {
            // Intentar obtener mensaje del RealJMSListener primero
            if (RealJMSListener.isInitialized()) {
                System.out.println("=== OBTENIENDO ÚLTIMO MENSAJE DEL REAL JMS LISTENER ===");
                String message = RealJMSListener.getLastMessage();
                
                if (message != null) {
                    System.out.println("Mensaje obtenido del JMS real: " + message.length() + " caracteres");
                    return message;
                }
            }
            
            // Fallback al SimpleTestListener
            System.out.println("=== OBTENIENDO ÚLTIMO MENSAJE DEL TEST LISTENER (FALLBACK) ===");
            String message = SimpleTestListener.getLastMessage();
            
            if (message != null) {
                System.out.println("Mensaje obtenido del test listener: " + message.length() + " caracteres");
                return message;
            } else {
                System.out.println("No hay mensajes disponibles en ningún listener");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensaje de los listeners: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene todos los mensajes almacenados en el listener activo
     * @return Lista de todos los mensajes recibidos
     */
    public List<String> getAllMessages() {
        try {
            // Intentar obtener mensajes del RealJMSListener primero
            if (RealJMSListener.isInitialized()) {
                System.out.println("=== OBTENIENDO TODOS LOS MENSAJES DEL REAL JMS LISTENER ===");
                List<String> messages = RealJMSListener.getAllMessages();
                System.out.println("Total mensajes obtenidos del JMS real: " + messages.size());
                return messages;
            }
            
            // Fallback al SimpleTestListener
            System.out.println("=== OBTENIENDO TODOS LOS MENSAJES DEL TEST LISTENER (FALLBACK) ===");
            List<String> messages = SimpleTestListener.getAllMessages();
            System.out.println("Total mensajes obtenidos del test listener: " + messages.size());
            return messages;
            
        } catch (Exception e) {
            System.err.println("Error obteniendo mensajes de los listeners: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Obtiene el estado del listener activo
     * @return String con información del estado
     */
    public String getListenerStatus() {
        try {
            if (RealJMSListener.isInitialized()) {
                return "REAL JMS: " + RealJMSListener.getListenerStatus();
            } else {
                return "TEST MODE: " + SimpleTestListener.getListenerStatus();
            }
        } catch (Exception e) {
            return "Error obteniendo estado del listener: " + e.getMessage();
        }
    }
    
    /**
     * Limpia todos los mensajes almacenados en el listener activo
     */
    public void clearMessages() {
        try {
            if (RealJMSListener.isInitialized()) {
                System.out.println("=== LIMPIANDO MENSAJES DEL REAL JMS LISTENER ===");
                RealJMSListener.clearMessages();
            } else {
                System.out.println("=== LIMPIANDO MENSAJES DEL TEST LISTENER ===");
                SimpleTestListener.clearMessages();
            }
        } catch (Exception e) {
            System.err.println("Error limpiando mensajes de los listeners: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica si hay mensajes disponibles en el listener activo
     * @return true si hay mensajes disponibles, false en caso contrario
     */
    public boolean hasMessages() {
        try {
            String lastMessage = getLastMessage();
            return lastMessage != null && !lastMessage.trim().isEmpty();
        } catch (Exception e) {
            System.err.println("Error verificando mensajes de los listeners: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el listener JMS real está inicializado
     * @return true si está inicializado, false en caso contrario
     */
    public boolean isRealJMSInitialized() {
        try {
            return RealJMSListener.isInitialized();
        } catch (Exception e) {
            System.err.println("Error verificando estado del JMS real: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el listener está inicializado (cualquiera de los dos)
     * @return true si está inicializado, false en caso contrario
     */
    public boolean isListenerInitialized() {
        try {
            return RealJMSListener.isInitialized() || SimpleTestListener.isInitialized();
        } catch (Exception e) {
            System.err.println("Error verificando estado de los listeners: " + e.getMessage());
            return false;
        }
    }
}