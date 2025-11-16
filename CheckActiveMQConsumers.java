import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.util.Enumeration;

/**
 * Script para verificar consumers activos y mensajes en la cola
 */
public class CheckActiveMQConsumers {
    
    private static final String BROKER_URL = "tcp://172.193.242.89:61616";
    private static final String QUEUE_NAME = "excel-input-queue";
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("=== VERIFICACION DE CONSUMERS Y MENSAJES ===");
        System.out.println("==========================================");
        System.out.println("Broker: " + BROKER_URL);
        System.out.println("Cola: " + QUEUE_NAME);
        System.out.println();
        
        Connection connection = null;
        Session session = null;
        
        try {
            // Conectar
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            
            System.out.println("✅ Conectado al broker");
            System.out.println();
            
            // Usar QueueBrowser para VER mensajes sin consumirlos
            System.out.println("--- USANDO QUEUEBROWSER (VER SIN CONSUMIR) ---");
            QueueBrowser browser = session.createBrowser(queue);
            Enumeration<?> messages = browser.getEnumeration();
            
            int messageCount = 0;
            while (messages.hasMoreElements()) {
                messageCount++;
                Message message = (Message) messages.nextElement();
                
                System.out.println();
                System.out.println("📨 MENSAJE #" + messageCount + " ENCONTRADO:");
                System.out.println("   Message ID: " + message.getJMSMessageID());
                System.out.println("   Timestamp: " + message.getJMSTimestamp());
                System.out.println("   Priority: " + message.getJMSPriority());
                System.out.println("   Redelivered: " + message.getJMSRedelivered());
                
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String text = textMessage.getText();
                    System.out.println("   Tipo: TextMessage");
                    System.out.println("   Longitud: " + text.length() + " caracteres");
                    System.out.println("   Contenido (primeros 200 chars):");
                    System.out.println("   " + text.substring(0, Math.min(200, text.length())));
                }
            }
            
            browser.close();
            
            if (messageCount == 0) {
                System.out.println("⚠️  No se encontraron mensajes con QueueBrowser");
                System.out.println("   Esto significa que la cola está vacía o");
                System.out.println("   todos los mensajes están siendo procesados por consumers");
            } else {
                System.out.println();
                System.out.println("✅ Total mensajes visibles: " + messageCount);
                System.out.println();
                System.out.println("⚠️  IMPORTANTE: Si QueueBrowser VE mensajes pero receive() NO los lee,");
                System.out.println("   significa que los mensajes están RESERVADOS por otro consumer");
                System.out.println("   (probablemente DirectJMSListener en WildFly)");
            }
            
            System.out.println();
            System.out.println("==========================================");
            System.out.println("=== RECOMENDACIONES ===");
            System.out.println("==========================================");
            System.out.println("1. Verifica en la consola web de ActiveMQ:");
            System.out.println("   - Ve a la cola " + QUEUE_NAME);
            System.out.println("   - Haz clic en 'View Consumers'");
            System.out.println("   - Verifica cuántos consumers hay activos");
            System.out.println();
            System.out.println("2. Si hay consumers activos pero no procesan:");
            System.out.println("   - El consumer puede estar 'bloqueado'");
            System.out.println("   - El MessageListener puede no estar funcionando");
            System.out.println("   - El polling manual puede no estar activo");
            System.out.println();
            System.out.println("3. Solución temporal:");
            System.out.println("   - Detén WildFly temporalmente");
            System.out.println("   - Ejecuta este script nuevamente");
            System.out.println("   - Debería poder leer el mensaje");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}

