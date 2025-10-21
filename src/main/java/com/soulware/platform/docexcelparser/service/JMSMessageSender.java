package com.soulware.platform.docexcelparser.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servicio para enviar mensajes a la cola JMS
 */
@ApplicationScoped
public class JMSMessageSender {

    private static final Logger logger = Logger.getLogger(JMSMessageSender.class.getName());
    private static final String QUEUE_NAME = "excel-input-queue";

    /**
     * Envía un mensaje a la cola JMS
     * @param messageContent Contenido del mensaje a enviar
     * @return true si se envió exitosamente, false en caso contrario
     */
    public boolean sendMessageToQueue(String messageContent) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            logger.info("=== ENVIANDO MENSAJE REAL A LA COLA JMS ===");
            System.out.println("=== ENVIANDO MENSAJE REAL A LA COLA JMS ===");
            System.out.println("Mensaje: " + messageContent);
            
            // Obtener ConnectionFactory
            InitialContext context = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup("java:/ConnectionFactory");
            
            // Crear conexión
            connection = connectionFactory.createConnection();
            connection.start();
            
            // Crear sesión
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // Obtener referencia a la cola existente usando JNDI lookup
            Queue queue;
            try {
                queue = (Queue) context.lookup("java:/jms/queue/excel-input-queue");
                logger.info("SUCCESS: Found existing queue via JNDI for sending");
                System.out.println("SUCCESS: Found existing queue via JNDI for sending");
            } catch (NamingException e) {
                logger.warning("Failed to lookup queue via JNDI, creating new queue: " + e.getMessage());
                System.out.println("WARNING: Failed to lookup queue via JNDI, creating new queue: " + e.getMessage());
                queue = session.createQueue(QUEUE_NAME);
            }
            
            // Crear producer
            producer = session.createProducer(queue);
            
            // Crear mensaje de texto
            TextMessage message = session.createTextMessage(messageContent);
            
            // Enviar mensaje
            producer.send(message);
            
            logger.info("=== MENSAJE ENVIADO EXITOSAMENTE A LA COLA ===");
            System.out.println("=== MENSAJE ENVIADO EXITOSAMENTE A LA COLA ===");
            System.out.println("Cola: " + QUEUE_NAME);
            System.out.println("Contenido: " + messageContent.length() + " caracteres");
            
            return true;
            
        } catch (JMSException | NamingException e) {
            logger.log(Level.SEVERE, "Error enviando mensaje a la cola: " + e.getMessage(), e);
            System.err.println("=== ERROR ENVIANDO MENSAJE A LA COLA ===");
            System.err.println("Error: " + e.getMessage());
            return false;
        } finally {
            // Cerrar recursos
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                logger.log(Level.SEVERE, "Error cerrando recursos JMS: " + e.getMessage(), e);
            }
        }
    }
}
