import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

/**
 * Script de diagnóstico que lee la cola de la misma manera que el proyecto
 * 
 * Este script replica exactamente la lógica de DirectJMSListener para diagnosticar problemas
 * 
 * Uso (Windows):
 *   RunDiagnoseQueueReader.bat
 * 
 * Uso (Linux/Mac):
 *   chmod +x RunDiagnoseQueueReader.sh
 *   ./RunDiagnoseQueueReader.sh
 * 
 * Uso manual:
 *   mvn compile
 *   java -cp "target/classes;target/DocExcelParser/WEB-INF/lib/*" DiagnoseQueueReader
 * 
 * O con Maven exec:
 *   mvn compile exec:java -Dexec.mainClass="DiagnoseQueueReader" -Dexec.classpathScope=compile
 */
public class DiagnoseQueueReader {
    
    // Misma configuración que ApplicationConfig
    private static final String BROKER_URL = "tcp://172.193.242.89:61616";
    private static final String QUEUE_NAME = "excel-input-queue";
    
    private static Connection connection;
    private static Session session;
    private static MessageConsumer consumer;
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("=== DIAGNÓSTICO DE LECTURA DE COLA ===");
        System.out.println("=== REPLICA LA LÓGICA DEL PROYECTO ===");
        System.out.println("==========================================");
        System.out.println("Broker: " + BROKER_URL);
        System.out.println("Cola: " + QUEUE_NAME);
        System.out.println("Timestamp: " + LocalDateTime.now());
        System.out.println("==========================================");
        System.out.println();
        
        try {
            // Paso 1: Crear ConnectionFactory (igual que DirectJMSListener)
            System.out.println("[PASO 1/6] Creando ActiveMQConnectionFactory...");
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            factory.setAlwaysSessionAsync(false);
            System.out.println("✅ ConnectionFactory creado");
            System.out.println("   Broker URL: " + BROKER_URL);
            System.out.println();
            
            // Paso 2: Crear conexión (igual que DirectJMSListener)
            System.out.println("[PASO 2/6] Creando conexión...");
            connection = factory.createConnection();
            String clientId = "DiagnoseClient-" + System.currentTimeMillis();
            connection.setClientID(clientId);
            System.out.println("✅ Conexión creada");
            System.out.println("   ClientID: " + clientId);
            System.out.println();
            
            // Configurar ExceptionListener (igual que DirectJMSListener)
            connection.setExceptionListener(new ExceptionListener() {
                @Override
                public void onException(JMSException exception) {
                    System.err.println();
                    System.err.println("❌❌❌ EXCEPCIÓN DE CONEXIÓN DETECTADA ❌❌❌");
                    System.err.println("Error: " + exception.getMessage());
                    System.err.println("Error Code: " + exception.getErrorCode());
                    System.err.println("Linked Exception: " + exception.getLinkedException());
                    exception.printStackTrace();
                }
            });
            System.out.println("✅ ExceptionListener configurado");
            System.out.println();
            
            // Paso 3: Crear sesión (igual que DirectJMSListener)
            System.out.println("[PASO 3/6] Creando sesión...");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("✅ Sesión creada");
            System.out.println("   Acknowledge Mode: AUTO_ACKNOWLEDGE");
            System.out.println("   Transacted: false");
            System.out.println();
            
            // Paso 4: Crear cola y consumer (igual que DirectJMSListener)
            System.out.println("[PASO 4/6] Creando cola y consumer...");
            Queue queue = session.createQueue(QUEUE_NAME);
            consumer = session.createConsumer(queue);
            System.out.println("✅ Consumer creado");
            System.out.println("   Queue Name: " + QUEUE_NAME);
            System.out.println();
            
            // Paso 5: Iniciar conexión (igual que DirectJMSListener)
            System.out.println("[PASO 5/6] Iniciando conexión...");
            connection.start();
            System.out.println("✅ Conexión iniciada");
            System.out.println();
            
            // Paso 6: Intentar leer mensajes (DOS MÉTODOS)
            System.out.println("[PASO 6/6] Intentando leer mensajes...");
            System.out.println();
            
            // MÉTODO 1: Polling Manual PRIMERO (sin MessageListener)
            System.out.println("--- MÉTODO 1: Polling Manual (sin MessageListener) ---");
            System.out.println("   Intentando leer mensajes con receive()...");
            System.out.println();
            
            boolean messageReceivedByPolling = false;
            System.out.println("   IMPORTANTE: Intentando leer mensaje que está visible en la consola web...");
            System.out.println();
            
            for (int i = 1; i <= 10; i++) {
                System.out.println("   Poll #" + i + " - Intentando recibir mensaje (timeout: 5 segundos)...");
                
                try {
                    MessageConsumer tempConsumer = session.createConsumer(queue);
                    System.out.println("      Consumer creado, esperando mensaje...");
                    
                    Message message = tempConsumer.receive(5000); // 5 segundos timeout
                    
                    if (message != null) {
                        System.out.println();
                        System.out.println("✅✅✅ MENSAJE RECIBIDO POR POLLING MANUAL ✅✅✅");
                        processMessage(message, "Polling Manual (Poll #" + i + ")");
                        messageReceivedByPolling = true;
                        tempConsumer.close();
                        break;
                    } else {
                        System.out.println("      ⚠️  No hay mensajes (timeout)");
                        System.out.println("      Posible causa: mensaje ya consumido por otro consumer");
                    }
                    
                    tempConsumer.close();
                    Thread.sleep(2000); // 2 segundos entre polls
                } catch (JMSException e) {
                    System.err.println("      ❌ Error en poll #" + i + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            if (!messageReceivedByPolling) {
                System.out.println();
                System.out.println("   ⚠️⚠️⚠️  NO SE PUDO LEER EL MENSAJE ⚠️⚠️⚠️");
                System.out.println("   Posibles causas:");
                System.out.println("   1. El mensaje ya fue consumido por otro consumer (DirectJMSListener en WildFly)");
                System.out.println("   2. El mensaje está bloqueado por otro consumer");
                System.out.println("   3. Hay un problema con el prefetch o la configuración del consumer");
                System.out.println();
                System.out.println("   SOLUCIÓN: Verifica si hay otros consumers activos en la consola web de ActiveMQ");
            }
            System.out.println();
            
            // MÉTODO 2: MessageListener (crear nuevo consumer)
            System.out.println("--- MÉTODO 2: MessageListener (como DirectJMSListener) ---");
            System.out.println("   Creando nuevo consumer para MessageListener...");
            MessageConsumer listenerConsumer = session.createConsumer(queue);
            final boolean[] messageReceived = {false};
            listenerConsumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    messageReceived[0] = true;
                    System.out.println();
                    System.out.println("✅✅✅ MENSAJE RECIBIDO POR MESSAGELISTENER ✅✅✅");
                    processMessage(message, "MessageListener");
                }
            });
            System.out.println("✅ MessageListener configurado");
            System.out.println("   Esperando 10 segundos para ver si se activa...");
            Thread.sleep(10000);
            
            if (!messageReceived[0]) {
                System.out.println("⚠️  MessageListener NO se activó en 10 segundos");
                System.out.println("   Esto puede indicar un problema con el MessageListener");
                System.out.println("   (Problema común en Azure/redes remotas)");
            }
            
            listenerConsumer.close();
            System.out.println();
            
            System.out.println();
            System.out.println("==========================================");
            System.out.println("=== RESUMEN DEL DIAGNÓSTICO ===");
            System.out.println("==========================================");
            System.out.println("✅ Conexión establecida correctamente");
            System.out.println("✅ Consumer creado correctamente");
            if (messageReceivedByPolling) {
                System.out.println("✅ Polling manual: MENSAJE ENCONTRADO");
            } else {
                System.out.println("⚠️  Polling manual: NO se encontraron mensajes");
            }
            if (messageReceived[0]) {
                System.out.println("✅ MessageListener: FUNCIONÓ correctamente");
            } else {
                System.out.println("⚠️  MessageListener: NO se activó (problema común en Azure/redes remotas)");
            }
            System.out.println();
            if (!messageReceivedByPolling && !messageReceived[0]) {
                System.out.println("⚠️  NO SE RECIBIERON MENSAJES:");
                System.out.println("  1. Verifica que haya mensajes en la cola (consola web de ActiveMQ)");
                System.out.println("  2. Verifica que el nombre de la cola sea correcto: " + QUEUE_NAME);
                System.out.println("  3. Los mensajes pueden haber sido consumidos por otro consumer");
            } else {
                System.out.println("✅ MENSAJE(S) RECIBIDO(S) EXITOSAMENTE");
                System.out.println("   La conexión funciona correctamente");
            }
            System.out.println("==========================================");
            
        } catch (JMSException e) {
            System.err.println();
            System.err.println("❌❌❌ ERROR JMS ❌❌❌");
            System.err.println("==========================================");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Linked Exception: " + e.getLinkedException());
            System.err.println("==========================================");
            System.err.println();
            System.err.println("Posibles causas:");
            System.err.println("  1. El broker no está accesible desde tu PC");
            System.err.println("  2. Firewall bloqueando el puerto 61616");
            System.err.println("  3. El broker requiere autenticación");
            System.err.println("  4. Problemas de red/NAT en Azure");
            System.err.println();
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println();
            System.err.println("❌❌❌ ERROR INESPERADO ❌❌❌");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            closeResources();
        }
    }
    
    /**
     * Procesa un mensaje (igual que processMessage en DirectJMSListener)
     */
    private static void processMessage(Message message, String source) {
        try {
            System.out.println("==========================================");
            System.out.println("=== PROCESANDO MENSAJE ===");
            System.out.println("Fuente: " + source);
            System.out.println("Timestamp: " + LocalDateTime.now());
            System.out.println("==========================================");
            
            String messageText = null;
            String messageId = null;
            
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                messageText = textMessage.getText();
                messageId = message.getJMSMessageID();
                
                System.out.println("Tipo: TextMessage");
                System.out.println("Message ID: " + messageId);
                System.out.println("Longitud: " + messageText.length() + " caracteres");
                System.out.println();
                System.out.println("--- CONTENIDO DEL MENSAJE ---");
                System.out.println(messageText);
                System.out.println("--- FIN DEL MENSAJE ---");
                System.out.println();
                
            } else if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(messageBytes);
                messageText = new String(messageBytes, "UTF-8");
                messageId = message.getJMSMessageID();
                
                System.out.println("Tipo: BytesMessage");
                System.out.println("Message ID: " + messageId);
                System.out.println("Longitud: " + messageBytes.length + " bytes");
                System.out.println();
                System.out.println("--- CONTENIDO DEL MENSAJE ---");
                System.out.println(messageText);
                System.out.println("--- FIN DEL MENSAJE ---");
                System.out.println();
            } else {
                System.out.println("Tipo: " + message.getClass().getName());
                System.out.println("⚠️  Tipo de mensaje no soportado para procesamiento");
                return;
            }
            
            // Intentar parsear como JSON (igual que DirectJMSListener)
            if (messageText != null && !messageText.trim().isEmpty()) {
                try {
                    System.out.println("--- ANÁLISIS JSON ---");
                    JsonNode jsonNode = objectMapper.readTree(messageText);
                    
                    System.out.println("✅ Mensaje es JSON válido");
                    System.out.println();
                    
                    // Extraer campos (igual que DirectJMSListener)
                    if (jsonNode.has("fileKey")) {
                        String fileKey = jsonNode.get("fileKey").asText();
                        System.out.println("📁 fileKey encontrado: " + fileKey);
                    } else {
                        System.out.println("⚠️  fileKey NO encontrado");
                    }
                    
                    if (jsonNode.has("fileName")) {
                        String fileName = jsonNode.get("fileName").asText();
                        System.out.println("📄 fileName: " + fileName);
                    }
                    
                    if (jsonNode.has("excelBase64")) {
                        String base64 = jsonNode.get("excelBase64").asText();
                        System.out.println("📦 excelBase64 encontrado: " + base64.length() + " caracteres");
                    } else {
                        System.out.println("⚠️  excelBase64 NO encontrado");
                    }
                    
                    if (jsonNode.has("bucket")) {
                        String bucket = jsonNode.get("bucket").asText();
                        System.out.println("🪣 bucket: " + bucket);
                    }
                    
                    if (jsonNode.has("messageId")) {
                        String jsonMessageId = jsonNode.get("messageId").asText();
                        System.out.println("🆔 messageId: " + jsonMessageId);
                    }
                    
                    System.out.println();
                    System.out.println("✅ El mensaje tiene la estructura esperada");
                    System.out.println("   El proyecto debería poder procesarlo correctamente");
                    
                } catch (Exception jsonException) {
                    System.out.println("⚠️  Error parseando JSON: " + jsonException.getMessage());
                    System.out.println("   El mensaje puede no estar en formato JSON esperado");
                }
            }
            
            System.out.println("==========================================");
            System.out.println("=== MENSAJE PROCESADO EXITOSAMENTE ===");
            System.out.println("==========================================");
            
        } catch (JMSException e) {
            System.err.println("❌ Error procesando mensaje: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error inesperado procesando mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cierra los recursos (igual que closeExistingConnections en DirectJMSListener)
     */
    private static void closeResources() {
        System.out.println();
        System.out.println("Cerrando recursos...");
        
        try {
            if (consumer != null) {
                consumer.close();
                System.out.println("✅ Consumer principal cerrado");
            }
        } catch (JMSException e) {
            System.err.println("⚠️  Error cerrando consumer: " + e.getMessage());
        }
        
        try {
            if (session != null) {
                session.close();
                System.out.println("✅ Sesión cerrada");
            }
        } catch (JMSException e) {
            System.err.println("⚠️  Error cerrando sesión: " + e.getMessage());
        }
        
        try {
            if (connection != null) {
                connection.close();
                System.out.println("✅ Conexión cerrada");
            }
        } catch (JMSException e) {
            System.err.println("⚠️  Error cerrando conexión: " + e.getMessage());
        }
        
        System.out.println("✅ Recursos cerrados correctamente");
    }
}

