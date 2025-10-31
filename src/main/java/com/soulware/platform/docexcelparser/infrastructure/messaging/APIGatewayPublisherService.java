package com.soulware.platform.docexcelparser.infrastructure.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.soulware.platform.docexcelparser.infrastructure.config.ApplicationConfig;

/**
 * Servicio para enviar links de Excel generados al API Gateway
 */
@ApplicationScoped
public class APIGatewayPublisherService {

    private static final Logger logger = Logger.getLogger(APIGatewayPublisherService.class.getName());
    private final ApplicationConfig config = new ApplicationConfig();
    private final ObjectMapper objectMapper;
    
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public APIGatewayPublisherService() {
        this.objectMapper = new ObjectMapper();
        // No inicializar JMS aquí, se hará lazy cuando se necesite
        System.out.println("=== APIGatewayPublisherService CONSTRUCTOR ===");
        System.out.println("✅ APIGatewayPublisherService creado (inicialización lazy)");
    }
    
    private void initJMSConnection() {
        try {
            System.out.println("=== INICIALIZANDO APIGatewayPublisherService (LAZY) ===");
            logger.info("Initializing APIGatewayPublisherService JMS connection (lazy)");
            
            String brokerUrl = config.getJmsBrokerUrl();
            String queueName = config.getJmsQueueExcelGeneratedLinks();
            
            // Usar ActiveMQConnectionFactory directamente como en PatientFormListener
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            // Crear la cola directamente
            Queue queue = session.createQueue(queueName);
            System.out.println("✅ Cola creada: " + queueName);
            
            producer = session.createProducer(queue);
            logger.info("APIGatewayPublisherService initialized and ready to publish to queue: " + queueName);
            System.out.println("✅ APIGatewayPublisherService inicializado correctamente");
            System.out.println("   Cola destino: " + queueName);
            System.out.println("   Producer creado exitosamente");
            
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Error initializing APIGatewayPublisherService: " + e.getMessage(), e);
            System.err.println("❌ Error inicializando APIGatewayPublisherService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía el link del Excel generado a la cola para el API Gateway
     * @param downloadUrl URL de descarga del Excel
     * @param fileName Nombre del archivo
     * @param messageId ID del mensaje original
     * @return true si se envió exitosamente
     */
    public boolean sendExcelLinkToAPIGateway(String downloadUrl, String fileName, String messageId) {
        try {
            String queueName = config.getJmsQueueExcelGeneratedLinks();
            logger.info("=== ENVIANDO LINK DE EXCEL A LA COLA PARA API GATEWAY ===");
            System.out.println("=== ENVIANDO LINK DE EXCEL A LA COLA PARA API GATEWAY ===");
            System.out.println("Cola destino: " + queueName);
            System.out.println("Download URL: " + downloadUrl);
            System.out.println("File Name: " + fileName);
            System.out.println("Message ID: " + messageId);
            
            // Inicializar conexión JMS si no está disponible
            if (producer == null || session == null) {
                System.out.println("🔄 Inicializando conexión JMS (lazy)...");
                initJMSConnection();
                
                // Verificar nuevamente después de la inicialización
                if (producer == null || session == null) {
                    System.err.println("❌ Error: Conexión JMS no disponible después de inicialización");
                    logger.severe("JMS connection not available after initialization");
                    return false;
                }
            }
            
            // Crear mensaje JSON
            ObjectNode messageJson = objectMapper.createObjectNode();
            messageJson.put("downloadUrl", downloadUrl);
            messageJson.put("fileName", fileName);
            messageJson.put("messageId", messageId);
            messageJson.put("timestamp", java.time.Instant.now().toString());
            messageJson.put("source", "excel-parser");
            messageJson.put("status", "generated");
            
            String messageContent = objectMapper.writeValueAsString(messageJson);
            System.out.println("📄 Contenido del mensaje JSON:");
            System.out.println(messageContent);
            
            // Crear mensaje JMS
            TextMessage jmsMessage = session.createTextMessage(messageContent);
            jmsMessage.setStringProperty("source", "excel-parser");
            jmsMessage.setStringProperty("messageType", "excel-generated-link");
            jmsMessage.setStringProperty("fileName", fileName);
            jmsMessage.setStringProperty("originalMessageId", messageId);
            
            System.out.println("📤 Enviando mensaje JMS...");
            
            // Enviar mensaje a la cola
            producer.send(jmsMessage);
            
            logger.info("=== LINK DE EXCEL ENVIADO EXITOSAMENTE A LA COLA ===");
            System.out.println("=== LINK DE EXCEL ENVIADO EXITOSAMENTE A LA COLA ===");
            System.out.println("Cola: " + queueName);
            System.out.println("JMS Message ID: " + jmsMessage.getJMSMessageID());
            System.out.println("Download URL: " + downloadUrl);
            System.out.println("Contenido del mensaje:");
            System.out.println(messageContent);
            return true;
            
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Error enviando link de Excel a la cola: " + e.getMessage(), e);
            System.err.println("=== ERROR ENVIANDO LINK DE EXCEL A LA COLA ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error general enviando link de Excel a la cola: " + e.getMessage(), e);
            System.err.println("=== ERROR GENERAL ENVIANDO LINK DE EXCEL A LA COLA ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cierra la conexión JMS
     */
    public void closeConnection() {
        try {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
            logger.info("APIGatewayPublisherService JMS connections closed");
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Error closing APIGatewayPublisherService JMS connections: " + e.getMessage(), e);
        }
    }
}
