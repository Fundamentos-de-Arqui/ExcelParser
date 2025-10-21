package com.soulware.platform.docexcelparser.listener;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.*;
import jakarta.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * WebListener para leer mensajes directamente de la cola JMS
 * Este listener lee mensajes de excel.input.queue y los almacena para consulta
 */
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "excel.input.queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
    }
)
public class QueueMessageListener implements MessageListener {
    
    // Almacenar mensajes leídos para consulta
    private static final Map<String, String> receivedMessages = new ConcurrentHashMap<>();
    private static final List<String> messageHistory = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        System.out.println("=== QUEUE MESSAGE LISTENER INICIALIZADO ===");
        System.out.println("Cola: excel.input.queue");
        System.out.println("Modo: Lectura directa JMS");
        System.out.println("==========================================");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("=== QUEUE MESSAGE LISTENER DESTRUIDO ===");
    }
    
    @Override
    public void onMessage(Message message) {
        try {
            System.out.println("=== MENSAJE RECIBIDO EN LISTENER ===");
            System.out.println("Tipo de mensaje: " + message.getClass().getSimpleName());
            System.out.println("Message ID: " + message.getJMSMessageID());
            System.out.println("Timestamp: " + message.getJMSTimestamp());
            
            String messageContent = null;
            
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                messageContent = textMessage.getText();
                System.out.println("Contenido (TextMessage): " + messageContent.length() + " caracteres");
            } else if (message instanceof ObjectMessage) {
                ObjectMessage objectMessage = (ObjectMessage) message;
                Object obj = objectMessage.getObject();
                messageContent = obj.toString();
                System.out.println("Contenido (ObjectMessage): " + messageContent.length() + " caracteres");
            } else if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(bytes);
                messageContent = new String(bytes);
                System.out.println("Contenido (BytesMessage): " + messageContent.length() + " caracteres");
            } else {
                messageContent = message.toString();
                System.out.println("Contenido (toString): " + messageContent.length() + " caracteres");
            }
            
            // Almacenar mensaje para consulta
            String messageId = message.getJMSMessageID();
            receivedMessages.put(messageId, messageContent);
            messageHistory.add(messageContent);
            
            // Mantener solo los últimos 10 mensajes
            if (messageHistory.size() > 10) {
                messageHistory.remove(0);
            }
            
            System.out.println("Mensaje almacenado con ID: " + messageId);
            System.out.println("Total mensajes almacenados: " + receivedMessages.size());
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("Error procesando mensaje en listener: " + e.getMessage());
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
        return "Listener activo - Mensajes recibidos: " + receivedMessages.size() + 
               " - Historial: " + messageHistory.size();
    }
    
    /**
     * Limpia todos los mensajes almacenados
     */
    public static void clearMessages() {
        receivedMessages.clear();
        messageHistory.clear();
        System.out.println("=== MENSAJES LIMPIADOS DEL LISTENER ===");
    }
}
