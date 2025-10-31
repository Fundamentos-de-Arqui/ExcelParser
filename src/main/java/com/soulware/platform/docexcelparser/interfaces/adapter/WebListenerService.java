package com.soulware.platform.docexcelparser.interfaces.adapter;

import com.soulware.platform.docexcelparser.interfaces.listener.DirectJMSListener;
import com.soulware.platform.docexcelparser.interfaces.listener.SimpleTestListener;
import com.soulware.platform.docexcelparser.domain.model.PatientProfile;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Servicio para obtener mensajes del listener JMS directo o fallback al test listener
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
            // Intentar obtener mensaje del DirectJMSListener primero
            if (DirectJMSListener.isInitialized()) {
                System.out.println("=== OBTENIENDO ÚLTIMO MENSAJE DEL DIRECT JMS LISTENER ===");
                String message = DirectJMSListener.getLastMessage();

                if (message != null) {
                    System.out.println("Mensaje obtenido del JMS directo: " + message.length() + " caracteres");
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
            // Intentar obtener mensajes del DirectJMSListener primero
            if (DirectJMSListener.isInitialized()) {
                System.out.println("=== OBTENIENDO TODOS LOS MENSAJES DEL DIRECT JMS LISTENER ===");
                List<String> messages = DirectJMSListener.getAllMessages();
                System.out.println("Total mensajes obtenidos del JMS directo: " + messages.size());
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
            if (DirectJMSListener.isInitialized()) {
                return "DIRECT JMS: " + DirectJMSListener.getListenerStatus();
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
            if (DirectJMSListener.isInitialized()) {
                System.out.println("=== LIMPIANDO MENSAJES DEL DIRECT JMS LISTENER ===");
                DirectJMSListener.clearMessages();
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
     * Verifica si el listener JMS directo está inicializado
     * @return true si está inicializado, false en caso contrario
     */
    public boolean isDirectJMSInitialized() {
        try {
            return DirectJMSListener.isInitialized();
        } catch (Exception e) {
            System.err.println("Error verificando estado del JMS directo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el listener está inicializado (cualquiera de los dos)
     * @return true si está inicializado, false en caso contrario
     */
    public boolean isListenerInitialized() {
        try {
            return DirectJMSListener.isInitialized() || SimpleTestListener.isInitialized();
        } catch (Exception e) {
            System.err.println("Error verificando estado de los listeners: " + e.getMessage());
            return false;
        }
    }

    /**
     * Limpia el último mensaje leído para permitir leer el siguiente
     */
    public void clearLastMessage() {
        try {
            if (DirectJMSListener.isInitialized()) {
                System.out.println("=== LIMPIANDO ÚLTIMO MENSAJE DEL DIRECT JMS LISTENER ===");
                DirectJMSListener.clearLastMessage();
            } else {
                System.out.println("=== LIMPIANDO ÚLTIMO MENSAJE DEL TEST LISTENER ===");
                SimpleTestListener.clearLastMessage();
            }
        } catch (Exception e) {
            System.err.println("Error limpiando último mensaje de los listeners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene todos los pacientes procesados
     */
    public List<PatientProfile> getProcessedPatients() {
        try {
            if (DirectJMSListener.isInitialized()) {
                System.out.println("=== OBTENIENDO PACIENTES PROCESADOS DEL DIRECT JMS LISTENER ===");
                List<PatientProfile> patients = DirectJMSListener.getProcessedPatients();
                System.out.println("Total pacientes obtenidos del JMS directo: " + patients.size());
                return patients;
            } else {
                System.out.println("=== NO HAY PACIENTES PROCESADOS EN MODO TEST ===");
                return List.of();
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo pacientes procesados: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Obtiene el último paciente procesado
     */
    public PatientProfile getLastProcessedPatient() {
        try {
            if (DirectJMSListener.isInitialized()) {
                System.out.println("=== OBTENIENDO ÚLTIMO PACIENTE PROCESADO DEL DIRECT JMS LISTENER ===");
                PatientProfile patient = DirectJMSListener.getLastProcessedPatient();
                if (patient != null) {
                    System.out.println("Último paciente obtenido: " + patient.getFirstNames() + " " + patient.getPaternalSurname());
                } else {
                    System.out.println("No hay pacientes procesados");
                }
                return patient;
            } else {
                System.out.println("=== NO HAY PACIENTES PROCESADOS EN MODO TEST ===");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo último paciente procesado: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Limpia los pacientes procesados
     */
    public void clearProcessedPatients() {
        try {
            if (DirectJMSListener.isInitialized()) {
                System.out.println("=== LIMPIANDO PACIENTES PROCESADOS DEL DIRECT JMS LISTENER ===");
                DirectJMSListener.clearProcessedPatients();
            } else {
                System.out.println("=== NO HAY PACIENTES PROCESADOS EN MODO TEST ===");
            }
        } catch (Exception e) {
            System.err.println("Error limpiando pacientes procesados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}