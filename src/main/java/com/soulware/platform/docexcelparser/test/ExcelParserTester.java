package com.soulware.platform.docexcelparser.test;

import com.soulware.platform.docexcelparser.entity.PatientProfile;
import com.soulware.platform.docexcelparser.parser.ExcelPatientParser;
import com.soulware.platform.docexcelparser.service.PatientDataService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase para testing del parser de Excel con base64 personalizado
 */
@ApplicationScoped
public class ExcelParserTester {

    @Inject
    private ExcelPatientParser excelParser;
    
    @Inject
    private PatientDataService patientDataService;

    /**
     * Testea el parser con un base64 personalizado
     * @param base64Content Base64 del archivo Excel a testear
     * @return Map con resultados detallados del test
     */
    public Map<String, Object> testParserWithBase64(String base64Content) {
        Map<String, Object> testResult = new HashMap<>();
        
        try {
            System.out.println("=== INICIANDO TEST DEL PARSER ===");
            System.out.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            System.out.println("Base64 length: " + base64Content.length());
            
            // Paso 1: Validar el base64
            Map<String, Object> validationResult = validateBase64(base64Content);
            testResult.put("validation", validationResult);
            
            if (!(Boolean) validationResult.get("isValid")) {
                testResult.put("success", false);
                testResult.put("error", "Base64 inválido: " + validationResult.get("error"));
                return testResult;
            }
            
            // Paso 2: Procesar con el servicio completo
            Map<String, Object> serviceResult = patientDataService.processPatientData(base64Content);
            testResult.put("serviceResult", serviceResult);
            
            // Paso 3: Procesar directamente con el parser
            PatientProfile patient = excelParser.parsePatientFromExcel(base64Content);
            testResult.put("parserResult", patient);
            
            // Paso 4: Análisis detallado del paciente
            Map<String, Object> analysisResult = analyzePatientData(patient);
            testResult.put("analysis", analysisResult);
            
            // Paso 5: Generar resumen
            String summary = generateTestSummary(validationResult, serviceResult, patient, analysisResult);
            testResult.put("summary", summary);
            
            testResult.put("success", true);
            testResult.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            System.out.println("=== TEST COMPLETADO EXITOSAMENTE ===");
            
        } catch (Exception e) {
            System.err.println("Error durante el test: " + e.getMessage());
            e.printStackTrace();
            
            testResult.put("success", false);
            testResult.put("error", "Error durante el test: " + e.getMessage());
            testResult.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }
        
        return testResult;
    }
    
    /**
     * Valida el base64 proporcionado
     */
    private Map<String, Object> validateBase64(String base64Content) {
        Map<String, Object> validation = new HashMap<>();
        
        try {
            // Validaciones básicas
            if (base64Content == null || base64Content.trim().isEmpty()) {
                validation.put("isValid", false);
                validation.put("error", "Base64 está vacío o es null");
                return validation;
            }
            
            // Limpiar el base64
            String cleanBase64 = base64Content.replaceAll("\\s+", "").replaceAll("[^A-Za-z0-9+/=]", "");
            
            // Verificar longitud mínima
            if (cleanBase64.length() < 100) {
                validation.put("isValid", false);
                validation.put("error", "Base64 demasiado corto (" + cleanBase64.length() + " caracteres)");
                return validation;
            }
            
            // Verificar formato base64
            if (!cleanBase64.matches("^[A-Za-z0-9+/]*={0,2}$")) {
                validation.put("isValid", false);
                validation.put("error", "Base64 contiene caracteres inválidos");
                return validation;
            }
            
            // Intentar decodificar
            byte[] decodedBytes;
            try {
                decodedBytes = java.util.Base64.getDecoder().decode(cleanBase64);
            } catch (IllegalArgumentException e) {
                validation.put("isValid", false);
                validation.put("error", "No se puede decodificar el base64: " + e.getMessage());
                return validation;
            }
            
            // Verificar tamaño de archivo
            if (decodedBytes.length < 1000) {
                validation.put("isValid", false);
                validation.put("error", "Archivo decodificado demasiado pequeño (" + decodedBytes.length + " bytes)");
                return validation;
            }
            
            // Verificar firma ZIP (archivos .xlsx)
            boolean isValidZip = isValidExcelFile(decodedBytes);
            
            validation.put("isValid", isValidZip);
            validation.put("originalLength", base64Content.length());
            validation.put("cleanLength", cleanBase64.length());
            validation.put("decodedBytes", decodedBytes.length);
            validation.put("isValidExcel", isValidZip);
            validation.put("fileSizeKB", String.format("%.2f", decodedBytes.length / 1024.0));
            
            if (!isValidZip) {
                validation.put("error", "El archivo no parece ser un Excel válido (.xlsx)");
            }
            
        } catch (Exception e) {
            validation.put("isValid", false);
            validation.put("error", "Error durante la validación: " + e.getMessage());
        }
        
        return validation;
    }
    
    /**
     * Valida si los bytes corresponden a un archivo Excel válido
     */
    private boolean isValidExcelFile(byte[] bytes) {
        if (bytes.length < 4) {
            return false;
        }
        
        // Verificar la firma ZIP (los archivos .xlsx son archivos ZIP)
        byte[] zipSignature1 = {0x50, 0x4B, 0x03, 0x04}; // ZIP local file header
        byte[] zipSignature2 = {0x50, 0x4B, 0x05, 0x06}; // ZIP central directory
        byte[] zipSignature3 = {0x50, 0x4B, 0x07, 0x08}; // ZIP end of central directory
        
        return startsWith(bytes, zipSignature1) || 
               startsWith(bytes, zipSignature2) || 
               startsWith(bytes, zipSignature3);
    }
    
    /**
     * Verifica si un array de bytes comienza con otro array de bytes
     */
    private boolean startsWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Analiza los datos del paciente extraídos
     */
    private Map<String, Object> analyzePatientData(PatientProfile patient) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Contar campos completados
        int totalFields = 0;
        int completedFields = 0;
        
        // Campos obligatorios
        String[] requiredFields = {
            "firstNames", "paternalSurname", "maternalSurname", 
            "identityDocumentNumber", "email", "phone", "currentAddress"
        };
        
        // Campos opcionales
        String[] optionalFields = {
            "birthPlace", "birthDate", "gender", "maritalStatus", 
            "district", "religion", "educationLevel", "occupation", 
            "currentEducationalInstitution"
        };
        
        // Analizar campos obligatorios
        Map<String, Object> requiredAnalysis = new HashMap<>();
        for (String field : requiredFields) {
            totalFields++;
            String value = getFieldValue(patient, field);
            boolean isCompleted = value != null && !value.trim().isEmpty() && !value.equals("N/A");
            if (isCompleted) completedFields++;
            
            requiredAnalysis.put(field, Map.of(
                "value", value != null ? value : "null",
                "completed", isCompleted
            ));
        }
        
        // Analizar campos opcionales
        Map<String, Object> optionalAnalysis = new HashMap<>();
        for (String field : optionalFields) {
            totalFields++;
            String value = getFieldValue(patient, field);
            boolean isCompleted = value != null && !value.trim().isEmpty() && !value.equals("N/A");
            if (isCompleted) completedFields++;
            
            optionalAnalysis.put(field, Map.of(
                "value", value != null ? value : "null",
                "completed", isCompleted
            ));
        }
        
        // Calcular porcentaje de completitud
        double completionPercentage = totalFields > 0 ? (double) completedFields / totalFields * 100 : 0;
        
        analysis.put("totalFields", totalFields);
        analysis.put("completedFields", completedFields);
        analysis.put("completionPercentage", String.format("%.1f%%", completionPercentage));
        analysis.put("requiredFields", requiredAnalysis);
        analysis.put("optionalFields", optionalAnalysis);
        analysis.put("hasErrors", patient.getCurrentAddress() != null && patient.getCurrentAddress().contains("Error"));
        
        return analysis;
    }
    
    /**
     * Obtiene el valor de un campo del paciente usando reflexión
     */
    private String getFieldValue(PatientProfile patient, String fieldName) {
        try {
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            java.lang.reflect.Method method = PatientProfile.class.getMethod(methodName);
            Object value = method.invoke(patient);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Genera un resumen detallado del test
     */
    private String generateTestSummary(Map<String, Object> validation, Map<String, Object> serviceResult, 
                                     PatientProfile patient, Map<String, Object> analysis) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("🧪 RESUMEN DEL TEST DEL PARSER\n");
        summary.append("═══════════════════════════════════════════════════════════\n");
        
        // Información de validación
        summary.append("📋 VALIDACIÓN DEL BASE64:\n");
        summary.append("   ✅ Válido: ").append(validation.get("isValid")).append("\n");
        summary.append("   📏 Longitud original: ").append(validation.get("originalLength")).append(" caracteres\n");
        summary.append("   📏 Longitud limpia: ").append(validation.get("cleanLength")).append(" caracteres\n");
        summary.append("   💾 Tamaño del archivo: ").append(validation.get("fileSizeKB")).append(" KB\n");
        summary.append("   📄 Es Excel válido: ").append(validation.get("isValidExcel")).append("\n");
        
        if (!(Boolean) validation.get("isValid")) {
            summary.append("   ❌ Error: ").append(validation.get("error")).append("\n");
        }
        
        summary.append("\n");
        
        // Resultado del servicio
        summary.append("🔧 RESULTADO DEL SERVICIO:\n");
        summary.append("   ✅ Éxito: ").append(serviceResult.get("success")).append("\n");
        if ((Boolean) serviceResult.get("success")) {
            summary.append("   📝 Mensaje: ").append(serviceResult.get("message")).append("\n");
        } else {
            summary.append("   ❌ Error: ").append(serviceResult.get("error")).append("\n");
        }
        
        summary.append("\n");
        
        // Análisis de datos
        summary.append("📊 ANÁLISIS DE DATOS EXTRAÍDOS:\n");
        summary.append("   📈 Completitud: ").append(analysis.get("completionPercentage")).append("\n");
        summary.append("   ✅ Campos completados: ").append(analysis.get("completedFields")).append("/").append(analysis.get("totalFields")).append("\n");
        summary.append("   ⚠️ Tiene errores: ").append(analysis.get("hasErrors")).append("\n");
        
        summary.append("\n");
        
        // Datos del paciente
        summary.append("👤 DATOS DEL PACIENTE:\n");
        summary.append("   👤 Nombre: ").append(patient.getFirstNames()).append(" ").append(patient.getPaternalSurname()).append(" ").append(patient.getMaternalSurname()).append("\n");
        summary.append("   🆔 Documento: ").append(patient.getIdentityDocumentNumber()).append("\n");
        summary.append("   📧 Email: ").append(patient.getEmail()).append("\n");
        summary.append("   📞 Teléfono: ").append(patient.getPhone()).append("\n");
        summary.append("   🏠 Dirección: ").append(patient.getCurrentAddress()).append("\n");
        summary.append("   🎂 Fecha Nacimiento: ").append(patient.getBirthDate()).append("\n");
        summary.append("   👫 Género: ").append(patient.getGender()).append("\n");
        summary.append("   💼 Ocupación: ").append(patient.getOccupation()).append("\n");
        
        summary.append("\n");
        
        // Recomendaciones
        summary.append("💡 RECOMENDACIONES:\n");
        double completionPercentage = Double.parseDouble(((String) analysis.get("completionPercentage")).replace("%", ""));
        
        if (completionPercentage >= 80) {
            summary.append("   ✅ Excelente: El parser extrajo la mayoría de los datos correctamente\n");
        } else if (completionPercentage >= 60) {
            summary.append("   ⚠️ Bueno: El parser extrajo datos importantes, pero algunos campos faltan\n");
        } else if (completionPercentage >= 40) {
            summary.append("   ⚠️ Regular: El parser extrajo algunos datos, pero muchos campos están vacíos\n");
        } else {
            summary.append("   ❌ Deficiente: El parser no pudo extraer la mayoría de los datos\n");
        }
        
        if ((Boolean) analysis.get("hasErrors")) {
            summary.append("   🔧 Revisar: Se detectaron errores en el procesamiento\n");
        }
        
        summary.append("═══════════════════════════════════════════════════════════\n");
        
        return summary.toString();
    }
    
    /**
     * Test rápido con base64 de ejemplo
     */
    public Map<String, Object> quickTest() {
        String exampleBase64 = "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";
        
        return testParserWithBase64(exampleBase64);
    }
}
