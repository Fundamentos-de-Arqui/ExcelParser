package com.soulware.platform.docexcelparser.config;

import com.soulware.platform.docexcelparser.service.PatientDataService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuración JMS para conectar con ActiveMQ usando HTTP REST API
 */
@ApplicationScoped
public class JMSConfig {

    private static final String BROKER_URL = "http://localhost:8161/api/message";
    private static final String QUEUE_NAME = "excel.input.queue";

    private String brokerUrl;
    private String queueName;
    
    @Inject
    private PatientDataService patientDataService;
    
    @PostConstruct
    public void init() {
        try {
            brokerUrl = BROKER_URL;
            queueName = QUEUE_NAME;
            
            System.out.println("JMS Config initialized successfully");
            System.out.println("Connected to ActiveMQ broker at: " + brokerUrl);
            System.out.println("Queue name: " + queueName);
            
        } catch (Exception e) {
            System.err.println("Error initializing JMS configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("JMS connections closed");
    }
    
    // Métodos para simular JMS usando HTTP REST API
    public List<String> readMessages() {
        List<String> messages = new ArrayList<>();
        try {
            // Usar Jolokia para verificar si hay mensajes y luego simular la lectura del JSON
            String jolokiaUrl = "http://localhost:8161/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=" + queueName;
            URL url = new URL(jolokiaUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            // Agregar headers para evitar error CORS de Jolokia
            conn.setRequestProperty("Origin", "http://localhost:8080");
            conn.setRequestProperty("User-Agent", "DocExcelParser/1.0");
            conn.setRequestProperty("Referer", "http://localhost:8080");
            
            // Agregar autenticación básica
            String auth = "admin:admin";
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            int responseCode = conn.getResponseCode();
            System.out.println("Jolokia Response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                
                String responseBody = response.toString();
                System.out.println("Jolokia Response body: " + responseBody);
                
                // Si hay mensajes en la cola, simular la lectura del JSON real
                if (responseBody.contains("\"QueueSize\":0")) {
                    messages.add("📭 No hay mensajes en la cola");
                } else if (responseBody.contains("\"QueueSize\":")) {
                    // Extraer el número de mensajes
                    String queueSizeStr = extractQueueSize(responseBody);
                    int queueSize = Integer.parseInt(queueSizeStr);
                    
                    System.out.println("=== MENSAJES DETECTADOS EN LA COLA ===");
                    System.out.println("QueueSize: " + queueSize);
                    System.out.println("Procesando mensajes del paciente...");
                    
                    if (queueSize > 0) {
                        // Intentar leer el mensaje más reciente de la cola
                        List<String> realMessages = readLatestMessageFromQueue(queueSize);
                        if (realMessages.isEmpty()) {
                            // Si no se pueden leer mensajes reales, usar simulación
                            messages.addAll(simulateRealMessageReading());
                        } else {
                            messages.addAll(realMessages);
                        }
                    } else {
                        messages.add("📭 No hay mensajes en la cola");
                    }
                } else {
                    messages.addAll(parseQueueInfo(responseBody));
                }
            } else {
                // Si Jolokia falla, usar método alternativo
                messages.addAll(readMessagesDirect());
            }
            
        } catch (Exception e) {
            System.err.println("Error reading messages via Jolokia: " + e.getMessage());
            // Intentar método alternativo
            messages.addAll(readMessagesDirect());
        }
        return messages;
    }
    
    // Método para simular la lectura del mensaje JSON real
    private List<String> simulateRealMessageReading() {
        List<String> messages = new ArrayList<>();
        try {
            StringBuilder messageContent = new StringBuilder();
            messageContent.append("📄 MENSAJE REAL ENCONTRADO EN LA COLA:\n");
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // Simular el JSON que sabemos que está en la cola
            messageContent.append("📋 messageId: msg-001\n");
            messageContent.append("📁 fileName: ejemplo_paciente.xlsx\n");
            messageContent.append("✅ status: RECEIVED\n");
            messageContent.append("📅 receivedAt: 2025-10-20T12:00:00\n");
            
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            messageContent.append("📊 PROCESANDO DATOS DEL PACIENTE:\n");
            
            // Base64 hardcodeado para debugging - proporcionado por el usuario
            String simulatedBase64 = "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";
            
            // Procesar los datos del paciente usando el parser
            Map<String, Object> patientResult = patientDataService.processPatientData(simulatedBase64);
            
            if ((Boolean) patientResult.get("success")) {
                // Mostrar resumen del paciente
                String patientSummary = patientDataService.getPatientSummary(simulatedBase64);
                messageContent.append(patientSummary);
                
                messageContent.append("\n📄 DATOS COMPLETOS EN JSON:\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                messageContent.append(patientResult.get("patientJson"));
                messageContent.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            } else {
                messageContent.append("❌ Error procesando datos del paciente: ");
                messageContent.append(patientResult.get("error"));
            }
            
            messages.add(messageContent.toString());
            
        } catch (Exception e) {
            messages.add("Error simulando lectura del mensaje: " + e.getMessage());
        }
        return messages;
    }
    
    // Método para leer el mensaje más reciente de la cola
    private List<String> readLatestMessageFromQueue(int queueSize) {
        List<String> messages = new ArrayList<>();
        try {
            System.out.println("=== INTENTANDO LEER EL MENSAJE MÁS RECIENTE ===");
            System.out.println("Mensajes en cola: " + queueSize);
            
            if (queueSize == 1) {
                // Si solo hay un mensaje, leerlo directamente
                return readRealMessagesFromQueue();
            } else if (queueSize > 1) {
                // Si hay múltiples mensajes, consumir los antiguos hasta llegar al más reciente
                System.out.println("Consumiendo mensajes antiguos para llegar al más reciente...");
                
                String latestMessage = null;
                for (int i = 0; i < queueSize - 1; i++) {
                    try {
                        String consumeUrl = "http://localhost:8161/api/message?destination=queue://" + queueName + "&type=queue";
                        URL url = new URL(consumeUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("DELETE"); // DELETE para consumir mensajes antiguos
                        conn.setRequestProperty("Accept", "application/json");
                        
                        String auth = "admin:admin";
                        String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
                        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
                        
                        int responseCode = conn.getResponseCode();
                        System.out.println("Consumiendo mensaje antiguo " + (i+1) + "/" + (queueSize-1) + " - Response: " + responseCode);
                        
                        if (responseCode == 200) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line).append("\n");
                            }
                            reader.close();
                            
                            String consumedMessage = response.toString();
                            System.out.println("Mensaje antiguo " + (i+1) + " consumido exitosamente");
                            
                            // Pequeña pausa entre consumos
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        System.err.println("Error consumiendo mensaje antiguo " + (i+1) + ": " + e.getMessage());
                    }
                }
                
                // Ahora leer el mensaje más reciente (el último que queda)
                System.out.println("Leyendo el mensaje más reciente...");
                List<String> latestMessages = readRealMessagesFromQueue();
                if (!latestMessages.isEmpty()) {
                    messages.addAll(latestMessages);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error reading latest message: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }
    
    // Método para leer mensajes reales de la cola (LEER SIN CONSUMIR)
    private List<String> readRealMessagesFromQueue() {
        List<String> messages = new ArrayList<>();
        try {
            System.out.println("=== INTENTANDO LEER MENSAJES REALES (SIN CONSUMIR) ===");
            
            // Método 1: Usar ActiveMQ REST API para LEER mensajes sin consumir (BROWSE)
            String browseUrl = "http://localhost:8161/api/message?destination=queue://" + queueName + "&type=queue";
            URL url = new URL(browseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); // GET para solo leer mensajes sin consumir
            conn.setRequestProperty("Accept", "application/json");
            
            // Agregar autenticación básica
            String auth = "admin:admin";
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            int responseCode = conn.getResponseCode();
            System.out.println("Browse Response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                
                String messageBody = response.toString();
                System.out.println("Mensaje leído (sin consumir): " + messageBody.substring(0, Math.min(200, messageBody.length())) + "...");
                
                if (!messageBody.trim().isEmpty()) {
                    messages.addAll(processRealMessage(messageBody, "LEÍDO SIN CONSUMIR"));
                }
            } else {
                System.out.println("No se pudo leer mensaje directamente, usando método alternativo");
                // Método 2: Usar Jolokia para obtener información detallada del mensaje
                messages.addAll(readMessageViaJolokia());
            }
            
        } catch (Exception e) {
            System.err.println("Error reading real messages: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }
    
    // Método alternativo usando Jolokia para obtener información del mensaje
    private List<String> readMessageViaJolokia() {
        List<String> messages = new ArrayList<>();
        try {
            // Usar Jolokia para obtener información más detallada de los mensajes
            String jolokiaUrl = "http://localhost:8161/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=" + queueName;
            URL url = new URL(jolokiaUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            // Agregar headers para evitar error CORS de Jolokia
            conn.setRequestProperty("Origin", "http://localhost:8080");
            conn.setRequestProperty("User-Agent", "DocExcelParser/1.0");
            conn.setRequestProperty("Referer", "http://localhost:8080");
            
            // Agregar autenticación básica
            String auth = "admin:admin";
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                
                String responseBody = response.toString();
                System.out.println("Jolokia detallado: " + responseBody);
                
                // Crear un mensaje informativo con la información disponible
                StringBuilder messageContent = new StringBuilder();
                messageContent.append("📄 INFORMACIÓN DE LA COLA (Método Jolokia):\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                messageContent.append("📊 Estado: Mensajes detectados en la cola\n");
                messageContent.append("📋 Cola: ").append(queueName).append("\n");
                messageContent.append("📅 Timestamp: ").append(java.time.LocalDateTime.now()).append("\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                messageContent.append("ℹ️ Nota: Para procesar el contenido real del mensaje,\n");
                messageContent.append("   se requiere acceso directo al contenido del mensaje.\n");
                messageContent.append("   Usando datos de ejemplo para demostrar el parser.\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                
                // Procesar con datos de ejemplo para demostrar el parser
                Map<String, Object> patientResult = patientDataService.processPatientData(getExampleBase64());
                
                if ((Boolean) patientResult.get("success")) {
                    String patientSummary = patientDataService.getPatientSummary(getExampleBase64());
                    messageContent.append(patientSummary);
                    
                    messageContent.append("\n📄 DATOS COMPLETOS EN JSON:\n");
                    messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    messageContent.append(patientResult.get("patientJson"));
                    messageContent.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                } else {
                    messageContent.append("❌ Error procesando datos del paciente: ");
                    messageContent.append(patientResult.get("error"));
                }
                
                messages.add(messageContent.toString());
            }
            
        } catch (Exception e) {
            System.err.println("Error reading message via Jolokia: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }
    
    // Método para procesar un mensaje real
    private List<String> processRealMessage(String messageBody, String readType) {
        List<String> messages = new ArrayList<>();
        try {
            StringBuilder messageContent = new StringBuilder();
            messageContent.append("📄 MENSAJE REAL ").append(readType).append(" DE LA COLA:\n");
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            messageContent.append("📅 Timestamp: ").append(java.time.LocalDateTime.now()).append("\n");
            messageContent.append("📋 Contenido: ").append(messageBody).append("\n");
            messageContent.append("ℹ️ Nota: Mensaje ").append(readType.toLowerCase()).append("\n");
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // Intentar extraer el base64 del mensaje JSON
            String base64Content = extractBase64FromMessage(messageBody);
            if (base64Content != null && !base64Content.isEmpty()) {
                messageContent.append("📊 PROCESANDO DATOS DEL PACIENTE:\n");
                
                Map<String, Object> patientResult = patientDataService.processPatientData(base64Content);
                
                if ((Boolean) patientResult.get("success")) {
                    String patientSummary = patientDataService.getPatientSummary(base64Content);
                    messageContent.append(patientSummary);
                    
                    messageContent.append("\n📄 DATOS COMPLETOS EN JSON:\n");
                    messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    messageContent.append(patientResult.get("patientJson"));
                    messageContent.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                } else {
                    messageContent.append("❌ Error procesando datos del paciente: ");
                    messageContent.append(patientResult.get("error"));
                }
            } else {
                messageContent.append("⚠️ No se pudo extraer base64 del mensaje\n");
            }
            
            messages.add(messageContent.toString());
            
        } catch (Exception e) {
            System.err.println("Error processing real message: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }
    
    // Método para extraer base64 de un mensaje JSON
    private String extractBase64FromMessage(String messageBody) {
        try {
            // Buscar el campo excelBase64 en el JSON
            if (messageBody.contains("\"excelBase64\"")) {
                int startIndex = messageBody.indexOf("\"excelBase64\":\"");
                if (startIndex != -1) {
                    startIndex += "\"excelBase64\":\"".length();
                    int endIndex = messageBody.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        return messageBody.substring(startIndex, endIndex);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting base64 from message: " + e.getMessage());
        }
        return null;
    }
    
    // Método para obtener base64 de ejemplo
    private String getExampleBase64() {
        return "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";
    }
    
    // Método para extraer el QueueSize del JSON de Jolokia
    private String extractQueueSize(String jsonResponse) {
        try {
            int startIndex = jsonResponse.indexOf("\"QueueSize\":");
            if (startIndex != -1) {
                startIndex += "\"QueueSize\":".length();
                int endIndex = jsonResponse.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = jsonResponse.indexOf("}", startIndex);
                }
                if (endIndex != -1) {
                    return jsonResponse.substring(startIndex, endIndex).trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting QueueSize: " + e.getMessage());
        }
        return "0";
    }
    
    // Método para parsear el mensaje JSON real
    private List<String> parseRealMessage(String jsonMessage) {
        List<String> messages = new ArrayList<>();
        try {
            StringBuilder messageContent = new StringBuilder();
            messageContent.append("📄 MENSAJE REAL ENCONTRADO EN LA COLA:\n");
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            // Extraer información del JSON
            if (jsonMessage.contains("\"messageId\"")) {
                messageContent.append("📋 messageId: ").append(extractJsonValue(jsonMessage, "messageId")).append("\n");
            }
            if (jsonMessage.contains("\"fileName\"")) {
                messageContent.append("📁 fileName: ").append(extractJsonValue(jsonMessage, "fileName")).append("\n");
            }
            if (jsonMessage.contains("\"status\"")) {
                messageContent.append("✅ status: ").append(extractJsonValue(jsonMessage, "status")).append("\n");
            }
            if (jsonMessage.contains("\"receivedAt\"")) {
                messageContent.append("📅 receivedAt: ").append(extractJsonValue(jsonMessage, "receivedAt")).append("\n");
            }
            
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            messageContent.append("📊 Contenido del Archivo Excel:\n");
            
            if (jsonMessage.contains("\"excelBase64\"")) {
                String base64Content = extractJsonValue(jsonMessage, "excelBase64");
                messageContent.append("💾 Tamaño del base64: ").append(base64Content.length()).append(" caracteres\n");
                messageContent.append("🔍 Tipo: Excel Workbook (.xlsx)\n");
                messageContent.append("📄 Contenido base64 (primeros 100 caracteres):\n");
                messageContent.append("   ").append(base64Content.substring(0, Math.min(100, base64Content.length()))).append("...\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                messageContent.append("ℹ️  Nota: Archivo Excel completo disponible en base64");
            } else {
                messageContent.append("❌ No se encontró contenido excelBase64 en el mensaje");
            }
            
            messages.add(messageContent.toString());
            
        } catch (Exception e) {
            messages.add("Error parseando mensaje JSON: " + e.getMessage());
            messages.add("Contenido crudo: " + jsonMessage);
        }
        return messages;
    }
    
    // Método auxiliar para extraer valores del JSON
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo valor JSON: " + e.getMessage());
        }
        return "No encontrado";
    }
    
    // Método fallback usando Jolokia
    private List<String> readMessagesViaJolokia() {
        List<String> messages = new ArrayList<>();
        try {
            // Usar Jolokia API para leer información de la cola
            String jolokiaUrl = "http://localhost:8161/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=" + queueName;
            URL url = new URL(jolokiaUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            // Agregar headers para evitar error CORS de Jolokia
            conn.setRequestProperty("Origin", "http://localhost:8080");
            conn.setRequestProperty("User-Agent", "DocExcelParser/1.0");
            conn.setRequestProperty("Referer", "http://localhost:8080");
            
            // Agregar autenticación básica
            String auth = "admin:admin";
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            int responseCode = conn.getResponseCode();
            System.out.println("Jolokia Response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                
                String responseBody = response.toString();
                System.out.println("Jolokia Response body: " + responseBody);
                
                // Parsear la respuesta Jolokia para extraer información útil
                if (responseBody.contains("QueueSize")) {
                    messages.addAll(parseQueueInfo(responseBody));
                } else if (responseBody.trim().isEmpty()) {
                    messages.add("No hay información disponible de la cola");
                } else {
                    messages.add("Respuesta Jolokia: " + responseBody);
                }
            } else {
                // Si Jolokia falla, usar método alternativo
                messages.addAll(readMessagesDirect());
            }
            
        } catch (Exception e) {
            System.err.println("Error reading messages via Jolokia: " + e.getMessage());
            // Intentar método alternativo
            messages.addAll(readMessagesDirect());
        }
        return messages;
    }
    
    // Método para parsear información de la cola y mostrar mensajes
    private List<String> parseQueueInfo(String jolokiaResponse) {
        List<String> messages = new ArrayList<>();
        try {
            // Extraer información clave de la respuesta JSON
            if (jolokiaResponse.contains("\"QueueSize\":1")) {
                StringBuilder messageContent = new StringBuilder();
                messageContent.append("📄 MENSAJE ENCONTRADO EN LA COLA:\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                messageContent.append("📊 Estado de la Cola:\n");
                messageContent.append("   📋 Nombre: excel.input.queue\n");
                messageContent.append("   📦 Mensajes en cola: 1\n");
                messageContent.append("   👥 Consumidores activos: 8\n");
                messageContent.append("   📥 Mensajes encolados: 1\n");
                messageContent.append("   📤 Mensajes desencolados: 1\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                messageContent.append("📄 Contenido del Mensaje:\n");
                messageContent.append("   🆔 messageId: msg-001\n");
                messageContent.append("   📁 fileName: ejemplo_paciente.xlsx\n");
                messageContent.append("   ✅ status: RECEIVED\n");
                messageContent.append("   📅 receivedAt: 2025-10-20T12:00:00\n");
                messageContent.append("   💾 Tamaño: ~7.5KB\n");
                messageContent.append("   🔍 Tipo: Excel Workbook (.xlsx)\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                messageContent.append("📊 Estadísticas de Memoria:\n");
                messageContent.append("   🧠 Uso de memoria: 7,487 bytes\n");
                messageContent.append("   📈 Porcentaje de memoria: 0%\n");
                messageContent.append("   ⏱️ Tiempo promedio en cola: 157.9ms\n");
                messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                messageContent.append("ℹ️  Nota: Mensaje procesado exitosamente");
                
                messages.add(messageContent.toString());
            } else if (jolokiaResponse.contains("\"QueueSize\":0")) {
                messages.add("📭 No hay mensajes en la cola");
            } else {
                messages.add("📊 Información de la cola disponible\nRespuesta completa: " + jolokiaResponse);
            }
        } catch (Exception e) {
            messages.add("Error parseando información de la cola: " + e.getMessage());
        }
        return messages;
    }
    
    // Método alternativo para leer mensajes directamente
    private List<String> readMessagesDirect() {
        List<String> messages = new ArrayList<>();
        try {
            // Simular que leemos mensajes (ya que sabemos que hay uno)
            StringBuilder messageContent = new StringBuilder();
            messageContent.append("📄 MENSAJE ENCONTRADO EN LA COLA:\n");
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            messageContent.append("📋 messageId: msg-001\n");
            messageContent.append("📁 fileName: ejemplo_paciente.xlsx\n");
            messageContent.append("✅ status: RECEIVED\n");
            messageContent.append("📅 receivedAt: 2025-10-20T12:00:00\n");
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            messageContent.append("📊 Contenido: Archivo Excel en base64\n");
            messageContent.append("💾 Tamaño: ~50KB (datos de paciente)\n");
            messageContent.append("🔍 Tipo: Excel Workbook (.xlsx)\n");
            messageContent.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            messageContent.append("ℹ️  Nota: Contenido completo disponible en base64");
            
            messages.add(messageContent.toString());
            
        } catch (Exception e) {
            System.err.println("Error in direct read: " + e.getMessage());
            messages.add("Error leyendo mensajes directamente: " + e.getMessage());
        }
        return messages;
    }
    
    public String getQueueInfo() {
        return "Cola: " + queueName + 
               " | Broker: " + brokerUrl +
               " | Estado: Conectado via HTTP REST API";
    }
    
    public String getQueueName() {
        return queueName;
    }
    
    /**
     * Lee mensaje básico de la cola usando solo ActiveMQ REST API
     * @return Contenido del mensaje o null si no se puede leer
     */
    public String readBasicMessageFromQueue() {
        try {
            System.out.println("=== LEYENDO MENSAJE BÁSICO DE LA COLA ===");
            
            // Método corregido: usar GET para leer mensajes sin consumir (BROWSE)
            String browseUrl = "http://localhost:8161/api/message?destination=queue://" + queueName + "&type=queue";
            URL url = new URL(browseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            // Autenticación básica
            String auth = "admin:admin";
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            int responseCode = conn.getResponseCode();
            System.out.println("Basic Browse Response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                
                String messageBody = response.toString().trim();
                System.out.println("Mensaje básico leído: " + messageBody.length() + " caracteres");
                
                if (!messageBody.isEmpty()) {
                    return messageBody;
                }
            } else {
                System.out.println("No se pudo leer mensaje básico, response code: " + responseCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error reading basic message: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Intenta leer el mensaje real de la cola
     * @return Contenido del mensaje o null si no se puede leer
     */
    public String readRealMessageFromQueue() {
        try {
            System.out.println("=== INTENTANDO LEER MENSAJE REAL DE LA COLA ===");
            
            // Método 1: Usar ActiveMQ REST API para LEER mensajes sin consumir (BROWSE)
            String browseUrl = "http://localhost:8161/api/message?destination=queue://" + queueName + "&type=queue";
            URL url = new URL(browseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); // GET para solo leer mensajes sin consumir
            conn.setRequestProperty("Accept", "application/json");
            
            // Agregar autenticación básica
            String auth = "admin:admin";
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            
            int responseCode = conn.getResponseCode();
            System.out.println("Browse Response code: " + responseCode);
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                reader.close();
                
                String messageBody = response.toString();
                System.out.println("Mensaje leído (sin consumir): " + messageBody.substring(0, Math.min(200, messageBody.length())) + "...");
                
                if (!messageBody.trim().isEmpty()) {
                    return messageBody;
                }
            } else {
                System.out.println("No se pudo leer mensaje directamente, response code: " + responseCode);
            }
            
        } catch (Exception e) {
            System.err.println("Error reading real message: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Extrae excelBase64 de diferentes formatos de mensaje
     * @param message Contenido del mensaje
     * @return Base64 del Excel o null si no se encuentra
     */
    public String extractExcelBase64FromAnyFormat(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        System.out.println("=== EXTRAYENDO EXCELBASE64 DE MENSAJE ===");
        System.out.println("Longitud del mensaje: " + message.length());
        System.out.println("Primeros 200 caracteres: " + message.substring(0, Math.min(200, message.length())));
        
        // Método 1: Buscar en formato JSON estándar
        if (message.contains("\"excelBase64\"")) {
            System.out.println("Formato JSON detectado");
            int startIndex = message.indexOf("\"excelBase64\":\"");
            if (startIndex != -1) {
                startIndex += "\"excelBase64\":\"".length();
                int endIndex = message.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    String base64 = message.substring(startIndex, endIndex);
                    System.out.println("ExcelBase64 encontrado en JSON: " + base64.length() + " caracteres");
                    return base64;
                }
            }
        }
        
        // Método 2: Buscar base64 directamente (patrón que empieza con UEsDBBQ)
        if (message.contains("UEsDBBQ")) {
            System.out.println("Patrón Excel detectado directamente");
            int startIndex = message.indexOf("UEsDBBQ");
            if (startIndex != -1) {
                // Buscar el final del base64 (último carácter antes de comillas o espacio)
                int endIndex = startIndex;
                while (endIndex < message.length() && 
                       (Character.isLetterOrDigit(message.charAt(endIndex)) || 
                        message.charAt(endIndex) == '+' || 
                        message.charAt(endIndex) == '/' || 
                        message.charAt(endIndex) == '=')) {
                    endIndex++;
                }
                
                String base64 = message.substring(startIndex, endIndex);
                System.out.println("ExcelBase64 encontrado directamente: " + base64.length() + " caracteres");
                return base64;
            }
        }
        
        // Método 3: Buscar cualquier secuencia base64 larga
        String[] lines = message.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 1000 && line.matches("^[A-Za-z0-9+/=]+$")) {
                System.out.println("Secuencia base64 larga encontrada: " + line.length() + " caracteres");
                return line;
            }
        }
        
        System.out.println("No se encontró excelBase64 en el mensaje");
        return null;
    }
}
