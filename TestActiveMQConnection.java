import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.util.Scanner;

/**
 * Script de prueba para verificar la conexión a ActiveMQ en Azure
 * 
 * Uso:
 * 1. Compilar: javac -cp "activemq-all-5.x.x.jar" TestActiveMQConnection.java
 * 2. Ejecutar: java -cp ".:activemq-all-5.x.x.jar" TestActiveMQConnection
 * 
 * O si tienes Maven:
 * mvn exec:java -Dexec.mainClass="TestActiveMQConnection" -Dexec.classpathScope=test
 */
public class TestActiveMQConnection {
    
    private static final String BROKER_URL = "tcp://172.193.242.89:61616";
    private static final String QUEUE_NAME = "excel-input-queue";
    
    public static void main(String[] args) {
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        
        System.out.println("==========================================");
        System.out.println("=== TEST DE CONEXIÓN A ACTIVEMQ ===");
        System.out.println("=== BROKER: " + BROKER_URL + " ===");
        System.out.println("=== COLA: " + QUEUE_NAME + " ===");
        System.out.println("==========================================");
        
        try {
            // 1. Crear ConnectionFactory
            System.out.println("\n[1/5] Creando ConnectionFactory...");
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            factory.setMaxThreadPoolSize(10);
            factory.setAlwaysSessionAsync(false);
            System.out.println("✅ ConnectionFactory creado");
            
            // 2. Crear conexión
            System.out.println("\n[2/5] Creando conexión...");
            connection = factory.createConnection();
            connection.setClientID("TestClient-" + System.currentTimeMillis());
            
            // Configurar exception listener
            connection.setExceptionListener(new ExceptionListener() {
                @Override
                public void onException(JMSException exception) {
                    System.err.println("\n❌ EXCEPCIÓN DE CONEXIÓN DETECTADA:");
                    System.err.println("   Error: " + exception.getMessage());
                    exception.printStackTrace();
                }
            });
            System.out.println("✅ Conexión creada - ClientID: " + connection.getClientID());
            
            // 3. Iniciar conexión
            System.out.println("\n[3/5] Iniciando conexión...");
            connection.start();
            System.out.println("✅ Conexión iniciada");
            
            // 4. Crear sesión y consumer
            System.out.println("\n[4/5] Creando sesión y consumer...");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            consumer = session.createConsumer(queue);
            System.out.println("✅ Consumer creado para cola: " + QUEUE_NAME);
            
            // 5. Intentar recibir mensajes
            System.out.println("\n[5/5] Esperando mensajes (timeout: 10 segundos)...");
            System.out.println("   (Presiona Enter para cancelar)");
            
            // Thread para cancelar con Enter
            Scanner scanner = new Scanner(System.in);
            Thread inputThread = new Thread(() -> {
                scanner.nextLine();
                System.out.println("\n⚠️  Cancelado por usuario");
            });
            inputThread.setDaemon(true);
            inputThread.start();
            
            // Intentar recibir mensaje
            Message message = consumer.receive(10000); // 10 segundos timeout
            
            if (message != null) {
                System.out.println("\n✅✅✅ MENSAJE RECIBIDO EXITOSAMENTE ✅✅✅");
                System.out.println("==========================================");
                System.out.println("Message ID: " + message.getJMSMessageID());
                System.out.println("Message Type: " + message.getClass().getSimpleName());
                
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String text = textMessage.getText();
                    System.out.println("Message Length: " + text.length() + " caracteres");
                    System.out.println("Message Content (primeros 200 chars):");
                    System.out.println(text.substring(0, Math.min(200, text.length())));
                } else if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    System.out.println("Message Length: " + bytesMessage.getBodyLength() + " bytes");
                }
                System.out.println("==========================================");
            } else {
                System.out.println("\n⚠️  No se recibieron mensajes en 10 segundos");
                System.out.println("   Esto puede significar:");
                System.out.println("   - La cola está vacía");
                System.out.println("   - La conexión funciona pero no hay mensajes");
                System.out.println("   - Verifica en la consola web de ActiveMQ si hay mensajes");
            }
            
            // Verificar estado de la conexión
            System.out.println("\n=== ESTADO DE LA CONEXIÓN ===");
            try {
                String clientId = connection.getClientID();
                System.out.println("✅ Conexión activa - ClientID: " + clientId);
            } catch (JMSException e) {
                System.err.println("❌ Error verificando conexión: " + e.getMessage());
            }
            
        } catch (JMSException e) {
            System.err.println("\n❌❌❌ ERROR DE CONEXIÓN ❌❌❌");
            System.err.println("==========================================");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("==========================================");
            System.err.println("\nPosibles causas:");
            System.err.println("1. El broker no está accesible desde tu PC");
            System.err.println("2. Firewall bloqueando el puerto 61616");
            System.err.println("3. El broker requiere autenticación");
            System.err.println("4. Problemas de red/NAT en Azure");
            System.err.println("\nVerificaciones:");
            System.err.println("- Ping al servidor: ping 172.193.242.89");
            System.err.println("- Telnet al puerto: telnet 172.193.242.89 61616");
            System.err.println("- Verificar reglas de firewall en Azure");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n❌ ERROR INESPERADO:");
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (consumer != null) consumer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
                System.out.println("\n✅ Recursos cerrados correctamente");
            } catch (JMSException e) {
                System.err.println("Error cerrando recursos: " + e.getMessage());
            }
        }
        
        System.out.println("\n==========================================");
        System.out.println("=== TEST COMPLETADO ===");
        System.out.println("==========================================");
    }
}

