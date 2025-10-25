import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestReverseParser {
    
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "excelParser_patientForm";
    
    public static void main(String[] args) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        
        try {
            System.out.println("=== PROBANDO REVERSE PARSER ===");
            System.out.println("Timestamp: " + java.time.LocalDateTime.now());
            System.out.println("");
            
            // Leer archivo JSON
            String jsonContent = Files.readString(Paths.get("patient_form_test.json"));
            System.out.println("📄 JSON leído exitosamente:");
            System.out.println("Longitud: " + jsonContent.length() + " caracteres");
            System.out.println("");
            
            // Conectar a ActiveMQ
            System.out.println("🔗 Conectando a ActiveMQ...");
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();
            
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);
            producer = session.createProducer(queue);
            
            // Crear mensaje
            TextMessage message = session.createTextMessage(jsonContent);
            
            // Enviar mensaje
            System.out.println("📤 Enviando mensaje a la cola: " + QUEUE_NAME);
            producer.send(message);
            
            System.out.println("✅ Mensaje enviado exitosamente!");
            System.out.println("");
            System.out.println("🔍 Monitoreando resultados...");
            System.out.println("1. Revisa los logs de WildFly");
            System.out.println("2. Verifica en Supabase Storage");
            System.out.println("3. Verifica en la cola excel-generated-links");
            System.out.println("");
            System.out.println("📋 Logs esperados en WildFly:");
            System.out.println("   === PATIENT FORM MESSAGE RECEIVED ===");
            System.out.println("   === PROCESANDO FORMULARIO DE PACIENTE ===");
            System.out.println("   ✅ Excel generado: XXXX bytes");
            System.out.println("   ✅ Excel subido a Supabase Storage: generated/XXXX.xlsx");
            System.out.println("   ✅ URL de descarga generada: https://...");
            System.out.println("   ✅ URL enviada al API Gateway exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (producer != null) producer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
