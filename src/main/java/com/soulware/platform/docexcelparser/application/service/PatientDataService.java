package com.soulware.platform.docexcelparser.application.service;

import com.soulware.platform.docexcelparser.domain.model.PatientProfile;
import com.soulware.platform.docexcelparser.infrastructure.parser.ExcelPatientParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para procesar datos de pacientes desde Excel
 */
@ApplicationScoped
public class PatientDataService {

    @Inject
    private ExcelPatientParser excelParser;

    private final ObjectMapper objectMapper;

    public PatientDataService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Procesa el contenido base64 de un Excel y extrae los datos del paciente
     * @param base64Content Contenido base64 del archivo Excel
     * @return Map con los datos del paciente procesados
     */
    public Map<String, Object> processPatientData(String base64Content) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Limpiar y validar el base64
            String cleanBase64 = cleanBase64Data(base64Content);
            System.out.println("Base64 limpio - Longitud: " + cleanBase64.length());
            System.out.println("Base64 limpio - Primeros 100 caracteres: " + cleanBase64.substring(0, Math.min(100, cleanBase64.length())));
            
            // Parsear el Excel y extraer datos del paciente
            PatientProfile patient = excelParser.parsePatientFromExcel(cleanBase64);
            
            // Crear JSON con los datos del paciente
            String patientJson = objectMapper.writeValueAsString(patient);
            
            result.put("success", true);
            result.put("patient", patient);
            result.put("patientJson", patientJson);
            result.put("message", "Datos del paciente extraídos exitosamente");
            
        } catch (Exception e) {
            System.err.println("Error processing patient data: " + e.getMessage());
            e.printStackTrace();
            
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "Error al procesar los datos del paciente");
        }
        
        return result;
    }
    
    /**
     * Limpia y valida el base64 removiendo caracteres inválidos y corrigiendo padding
     */
    private String cleanBase64Data(String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 data is null or empty");
        }
        
        System.out.println("=== LIMPIANDO BASE64 ===");
        System.out.println("Base64 original length: " + base64Data.length());
        System.out.println("Base64 original (primeros 100 chars): " + base64Data.substring(0, Math.min(100, base64Data.length())));
        
        // Remover espacios en blanco, saltos de línea y caracteres no válidos
        String cleaned = base64Data.replaceAll("\\s+", "")
                                  .replaceAll("[^A-Za-z0-9+/=]", "");
        
        System.out.println("Base64 después de limpiar: " + cleaned.length() + " chars");
        System.out.println("Base64 limpio (primeros 100 chars): " + cleaned.substring(0, Math.min(100, cleaned.length())));
        
        // Verificar que la longitud sea múltiplo de 4
        int remainder = cleaned.length() % 4;
        if (remainder != 0) {
            // Agregar padding si es necesario
            int paddingNeeded = 4 - remainder;
            cleaned += "====".substring(0, paddingNeeded);
            System.out.println("Padding agregado: " + paddingNeeded + " caracteres");
        }
        
        // Verificar que contenga solo caracteres válidos de base64
        if (!cleaned.matches("^[A-Za-z0-9+/]*={0,2}$")) {
            System.err.println("Base64 contiene caracteres inválidos después de limpiar");
            System.err.println("Base64 problemático: " + cleaned.substring(0, Math.min(200, cleaned.length())));
            throw new IllegalArgumentException("Invalid base64 characters found after cleaning");
        }
        
        // Validar que el base64 tenga al menos un tamaño mínimo razonable
        if (cleaned.length() < 100) {
            throw new IllegalArgumentException("Base64 too short, likely invalid");
        }
        
        System.out.println("Base64 final length: " + cleaned.length());
        System.out.println("Base64 final (primeros 100 chars): " + cleaned.substring(0, Math.min(100, cleaned.length())));
        System.out.println("=== BASE64 LIMPIO EXITOSAMENTE ===");
        
        return cleaned;
    }

    /**
     * Obtiene información resumida del paciente
     * @param base64Content Contenido base64 del archivo Excel
     * @return String con información resumida
     */
    public String getPatientSummary(String base64Content) {
        try {
            // Limpiar y validar el base64
            String cleanBase64 = cleanBase64Data(base64Content);
            PatientProfile patient = excelParser.parsePatientFromExcel(cleanBase64);
            
            StringBuilder summary = new StringBuilder();
            summary.append("📋 RESUMEN DEL PACIENTE:\n");
            summary.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            summary.append("👤 Nombre Completo: ");
            summary.append(patient.getFirstNames()).append(" ");
            summary.append(patient.getPaternalSurname()).append(" ");
            summary.append(patient.getMaternalSurname()).append("\n");
            
            summary.append("🆔 Documento: ").append(patient.getIdentityDocumentNumber()).append("\n");
            summary.append("📧 Email: ").append(patient.getEmail()).append("\n");
            summary.append("📞 Teléfono: ").append(patient.getPhone()).append("\n");
            summary.append("🏠 Dirección: ").append(patient.getCurrentAddress()).append("\n");
            summary.append("🎂 Fecha de Nacimiento: ").append(patient.getBirthDate()).append("\n");
            summary.append("👫 Género: ").append(patient.getGender()).append("\n");
            summary.append("💼 Ocupación: ").append(patient.getOccupation()).append("\n");
            summary.append("🏛️ Religión: ").append(patient.getReligion()).append("\n");
            summary.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            return summary.toString();
            
        } catch (Exception e) {
            return "❌ Error al generar resumen del paciente: " + e.getMessage();
        }
    }
}
