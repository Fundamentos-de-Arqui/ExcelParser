import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Test simple para verificar si receive() funciona directamente
 */
public class TestDirectReceive {
    public static void main(String[] args) {
        String brokerUrl = "tcp://172.193.242.89:61616?jms.prefetchPolicy.queuePrefetch=1";
        String queueName = "excel-input-queue";
        
        System.out.println("=== TEST DIRECT RECEIVE ===");
        System.out.println("Broker: " + brokerUrl);
        System.out.println("Queue: " + queueName);
        System.out.println();
        
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setAlwaysSessionAsync(false);
            
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            
            System.out.println("✅ Conectado");
            System.out.println("Creando consumer con prefetch=1...");
            
            consumer = session.createConsumer(queue);
            
            System.out.println("✅ Consumer creado");
            System.out.println("Intentando recibir mensaje (timeout: 10 segundos)...");
            System.out.println();
            
            Message message = consumer.receive(10000);
            
            if (message != null) {
                System.out.println("✅✅✅ MENSAJE RECIBIDO ✅✅✅");
                System.out.println("Message ID: " + message.getJMSMessageID());
                
                if (message instanceof TextMessage) {
                    TextMessage tm = (TextMessage) message;
                    String text = tm.getText();
                    System.out.println("Contenido (primeros 200 chars):");
                    System.out.println(text.substring(0, Math.min(200, text.length())));
                }
            } else {
                System.out.println("⚠️  No se recibió mensaje en 10 segundos");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (consumer != null) consumer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

