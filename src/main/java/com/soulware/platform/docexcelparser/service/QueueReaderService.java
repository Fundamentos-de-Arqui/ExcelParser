package com.soulware.platform.docexcelparser.service;

import com.soulware.platform.docexcelparser.config.JMSConfig;
import com.soulware.platform.docexcelparser.entity.PatientProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para leer mensajes de la cola excel.input.queue usando HTTP REST API
 */
@ApplicationScoped
public class QueueReaderService {
    
    @Inject
    private JMSConfig jmsConfig;
    
    @Inject
    private com.soulware.platform.docexcelparser.service.PatientDataService patientDataService;
    
    /**
     * Lee todos los mensajes disponibles en la cola
     * @return Lista de mensajes leídos
     */
    public List<String> readAllMessages() {
        List<String> messages = jmsConfig.readMessages();
        
        if (messages.isEmpty()) {
            messages.add("No hay mensajes disponibles en la cola.");
        } else {
            System.out.println("Mensajes leídos de la cola: " + messages.size());
        }
        
        return messages;
    }
    
    /**
     * Lee un solo mensaje de la cola
     * @return El primer mensaje disponible o null si no hay mensajes
     */
    public String readSingleMessage() {
        List<String> messages = jmsConfig.readMessages();
        
        if (messages.isEmpty()) {
            return "No hay mensajes disponibles";
        }
        
        String message = messages.get(0);
        System.out.println("Mensaje leído: " + message);
        return message;
    }
    
    /**
     * Verifica si hay mensajes en la cola sin leerlos
     * @return true si hay mensajes disponibles
     */
    public boolean hasMessages() {
        List<String> messages = jmsConfig.readMessages();
        return !messages.isEmpty();
    }
    
    /**
     * Obtiene información sobre la cola
     * @return Información de la cola
     */
    public String getQueueInfo() {
        return jmsConfig.getQueueInfo();
    }
    
    /**
     * Procesa mensajes de la cola - DESHABILITADO COMPLETAMENTE
     * @return Lista vacía ya que el procesamiento está deshabilitado
     */
    public List<Map<String, Object>> processQueueMessages() {
        System.out.println("=== PROCESAMIENTO DE MENSAJES DESHABILITADO ===");
        System.out.println("Solo lectura de cola activa - Sin parsing ni procesamiento");
        return new ArrayList<>();
    }
    
    /**
     * Procesa un mensaje individual y extrae datos del paciente si contiene excelBase64
     * @param message Mensaje de la cola
     * @return Map con datos del paciente procesado o null si no hay excelBase64
     */
    private Map<String, Object> processMessageWithExcelBase64(String message) {
        try {
            System.out.println("=== PROCESANDO MENSAJE INDIVIDUAL MEJORADO ===");
            System.out.println("Mensaje length: " + message.length());
            
            // Primero intentar leer el mensaje real de la cola
            String realMessage = jmsConfig.readRealMessageFromQueue();
            if (realMessage != null && !realMessage.trim().isEmpty()) {
                System.out.println("Mensaje real encontrado, procesando...");
                message = realMessage;
            }
            
            // Buscar excelBase64 en el mensaje usando métodos mejorados
            String excelBase64 = extractExcelBase64FromMessage(message);
            
            if (excelBase64 != null && !excelBase64.trim().isEmpty()) {
                System.out.println("ExcelBase64 encontrado - Longitud: " + excelBase64.length());
                
                // Procesar el Excel y extraer datos del paciente
                Map<String, Object> patientResult = patientDataService.processPatientData(excelBase64);
                
                if ((Boolean) patientResult.get("success")) {
                    PatientProfile patient = (PatientProfile) patientResult.get("patient");
                    
                    // Crear resultado estructurado
                    Map<String, Object> result = new HashMap<>();
                    result.put("messageId", extractMessageId(message));
                    result.put("fileName", extractFileName(message));
                    result.put("status", "PROCESSED");
                    result.put("processedAt", java.time.LocalDateTime.now().toString());
                    result.put("patient", patient);
                    result.put("patientSummary", patientDataService.getPatientSummary(excelBase64));
                    result.put("success", true);
                    
                    System.out.println("Paciente procesado exitosamente: " + 
                                     patient.getFirstNames() + " " + patient.getPaternalSurname());
                    
                    return result;
                } else {
                    System.out.println("Error procesando paciente: " + patientResult.get("error"));
                    
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("messageId", extractMessageId(message));
                    errorResult.put("fileName", extractFileName(message));
                    errorResult.put("status", "ERROR");
                    errorResult.put("error", patientResult.get("error"));
                    errorResult.put("success", false);
                    
                    return errorResult;
                }
            } else {
                System.out.println("No se encontró excelBase64 en el mensaje");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando mensaje individual: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extrae el excelBase64 de un mensaje usando métodos mejorados
     * @param message Mensaje de la cola
     * @return Base64 del Excel o null si no se encuentra
     */
    private String extractExcelBase64FromMessage(String message) {
        try {
            System.out.println("=== EXTRAYENDO EXCELBASE64 MEJORADO ===");
            
            // Usar el método mejorado del JMSConfig
            String base64 = jmsConfig.extractExcelBase64FromAnyFormat(message);
            
            if (base64 != null) {
                System.out.println("ExcelBase64 extraído exitosamente: " + base64.length() + " caracteres");
                return base64;
            } else {
                System.out.println("No se pudo extraer excelBase64 del mensaje");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error extrayendo excelBase64 del mensaje: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrae el messageId de un mensaje JSON
     * @param message Mensaje de la cola
     * @return MessageId o "unknown" si no se encuentra
     */
    private String extractMessageId(String message) {
        try {
            if (message.contains("\"messageId\"")) {
                int startIndex = message.indexOf("\"messageId\":\"");
                if (startIndex != -1) {
                    startIndex += "\"messageId\":\"".length();
                    int endIndex = message.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        return message.substring(startIndex, endIndex);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo messageId: " + e.getMessage());
        }
        return "unknown";
    }
    
    /**
     * Extrae el fileName de un mensaje JSON
     * @param message Mensaje de la cola
     * @return FileName o "unknown" si no se encuentra
     */
    private String extractFileName(String message) {
        try {
            if (message.contains("\"fileName\"")) {
                int startIndex = message.indexOf("\"fileName\":\"");
                if (startIndex != -1) {
                    startIndex += "\"fileName\":\"".length();
                    int endIndex = message.indexOf("\"", startIndex);
                    if (endIndex != -1) {
                        return message.substring(startIndex, endIndex);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error extrayendo fileName: " + e.getMessage());
        }
        return "unknown";
    }
    
    /**
     * Lee el mensaje simple directamente de la cola SIN PROCESAMIENTO
     * @return String con el JSON crudo o null si no hay mensajes
     */
    public String readSimpleMessageFromQueue() {
        try {
            System.out.println("=== LEYENDO MENSAJE SIMPLE DE LA COLA (SIN PROCESAMIENTO) ===");
            
            // Usar solo el método básico de lectura
            String simpleMessage = jmsConfig.readBasicMessageFromQueue();
            
            if (simpleMessage != null && !simpleMessage.trim().isEmpty()) {
                System.out.println("Mensaje simple leído exitosamente: " + simpleMessage.length() + " caracteres");
                return simpleMessage;
            } else {
                System.out.println("No se pudo leer mensaje simple de la cola");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error leyendo mensaje simple: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lee el mensaje crudo directamente de la cola para debugging
     * @return String con el JSON crudo o null si no hay mensajes
     */
    public String readRawMessageFromQueue() {
        try {
            System.out.println("=== LEYENDO MENSAJE CRUDO DE LA COLA ===");
            
            // Usar el método directo del JMSConfig
            String rawMessage = jmsConfig.readRealMessageFromQueue();
            
            if (rawMessage != null && !rawMessage.trim().isEmpty()) {
                System.out.println("Mensaje crudo leído exitosamente: " + rawMessage.length() + " caracteres");
                return rawMessage;
            } else {
                System.out.println("No se pudo leer mensaje crudo de la cola");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Error leyendo mensaje crudo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Obtiene un resumen de todos los pacientes procesados - DESHABILITADO
     * @return String con resumen de pacientes
     */
    public String getProcessedPatientsSummary() {
        return "🔧 PARSER COMPLETAMENTE DESHABILITADO - Solo lectura de cola activa";
    }
}
