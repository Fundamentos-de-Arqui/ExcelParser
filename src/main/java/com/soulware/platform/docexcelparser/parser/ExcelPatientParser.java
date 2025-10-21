package com.soulware.platform.docexcelparser.parser;

import com.soulware.platform.docexcelparser.entity.PatientProfile;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Parser para extraer datos de pacientes desde archivos Excel en base64
 */
@ApplicationScoped
public class ExcelPatientParser {

    /**
     * Parsea un archivo Excel desde base64 y extrae los datos del paciente
     * @param base64Content Contenido base64 del archivo Excel
     * @return PatientProfile con los datos extraídos
     */
    public PatientProfile parsePatientFromExcel(String base64Content) {
        try {
            System.out.println("=== INICIANDO PARSING DE EXCEL ===");
            System.out.println("Base64 length: " + base64Content.length());
            System.out.println("Base64 (primeros 100 chars): " + base64Content.substring(0, Math.min(100, base64Content.length())));
            
            // Validar que el base64 no esté vacío
            if (base64Content == null || base64Content.trim().isEmpty()) {
                throw new IllegalArgumentException("Base64 content is null or empty");
            }
            
            // Decodificar base64 a bytes con manejo de errores específicos
            byte[] excelBytes;
            try {
                excelBytes = Base64.getDecoder().decode(base64Content);
                System.out.println("Excel bytes decoded successfully: " + excelBytes.length + " bytes");
            } catch (IllegalArgumentException e) {
                System.err.println("Error decodificando Base64: " + e.getMessage());
                System.err.println("Base64 problemático: " + base64Content.substring(0, Math.min(200, base64Content.length())));
                throw new IllegalArgumentException("Invalid Base64 format: " + e.getMessage(), e);
            }
            
            // Validar que los bytes decodificados tengan un tamaño mínimo razonable
            if (excelBytes.length < 1000) {
                throw new IllegalArgumentException("Decoded bytes too small (" + excelBytes.length + " bytes), likely not a valid Excel file");
            }
            
            // Validar que los primeros bytes sean de un archivo Excel válido
            if (!isValidExcelFile(excelBytes)) {
                throw new IllegalArgumentException("Decoded bytes do not appear to be a valid Excel file");
            }
            
            // Crear workbook desde bytes con manejo de errores específicos
            Workbook workbook;
            try {
                workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes));
                System.out.println("Workbook created successfully");
                System.out.println("Number of sheets: " + workbook.getNumberOfSheets());
            } catch (Exception e) {
                System.err.println("Error creating workbook from bytes: " + e.getMessage());
                
                // Intentar métodos alternativos para archivos corruptos
                if (e.getMessage().contains("invalid distance too far back") || 
                    e.getMessage().contains("corrupted") ||
                    e.getMessage().contains("compression")) {
                    
                    System.out.println("=== INTENTANDO RECUPERACIÓN DE ARCHIVO CORRUPTO ===");
                    
                    // Método 1: Intentar con HSSFWorkbook (formato .xls)
                    try {
                        System.out.println("Intentando con formato .xls (HSSF)...");
                        workbook = new HSSFWorkbook(new ByteArrayInputStream(excelBytes));
                        System.out.println("Workbook HSSF creado exitosamente");
                    } catch (Exception e2) {
                        System.out.println("HSSF también falló: " + e2.getMessage());
                        
                        // Método 2: Intentar reparar el ZIP
                        try {
                            System.out.println("Intentando reparar archivo ZIP...");
                            byte[] repairedBytes = repairZipFile(excelBytes);
                            if (repairedBytes != null) {
                                workbook = new XSSFWorkbook(new ByteArrayInputStream(repairedBytes));
                                System.out.println("Archivo ZIP reparado exitosamente");
                            } else {
                                throw new IllegalArgumentException("No se pudo reparar el archivo ZIP");
                            }
                        } catch (Exception e3) {
                            System.err.println("Reparación ZIP falló: " + e3.getMessage());
                            throw new IllegalArgumentException("Archivo Excel corrupto o incompleto. Error: " + e.getMessage(), e);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Cannot create Excel workbook from decoded bytes: " + e.getMessage(), e);
                }
            }
            
            // Validar que el workbook tenga al menos una hoja
            if (workbook.getNumberOfSheets() == 0) {
                workbook.close();
                throw new IllegalArgumentException("Excel file has no sheets");
            }
            
            Sheet sheet = workbook.getSheetAt(0); // Primera hoja
            System.out.println("Sheet name: " + sheet.getSheetName());
            System.out.println("Sheet rows: " + (sheet.getLastRowNum() + 1));
            System.out.println("Sheet columns: " + sheet.getRow(0) != null ? sheet.getRow(0).getLastCellNum() : 0);
            
            // Crear objeto PatientProfile
            PatientProfile patient = new PatientProfile();
            
            // Extraer datos del formulario médico
            extractPatientData(sheet, patient);
            
            workbook.close();
            System.out.println("=== PARSING COMPLETADO EXITOSAMENTE ===");
            return patient;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error parsing Excel file: " + e.getMessage());
            return createDefaultPatient("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error parsing Excel file: " + e.getMessage());
            e.printStackTrace();
            return createDefaultPatient("Unexpected Error: " + e.getMessage());
        }
    }
    
    /**
     * Valida si los bytes decodificados corresponden a un archivo Excel válido
     */
    private boolean isValidExcelFile(byte[] bytes) {
        if (bytes.length < 4) {
            return false;
        }
        
        // Verificar la firma ZIP (los archivos .xlsx son archivos ZIP)
        // ZIP signature: 50 4B 03 04 o 50 4B 05 06 o 50 4B 07 08
        byte[] zipSignature1 = {0x50, 0x4B, 0x03, 0x04}; // ZIP local file header
        byte[] zipSignature2 = {0x50, 0x4B, 0x05, 0x06}; // ZIP central directory
        byte[] zipSignature3 = {0x50, 0x4B, 0x07, 0x08}; // ZIP end of central directory
        
        boolean isValidZip = startsWith(bytes, zipSignature1) || 
                            startsWith(bytes, zipSignature2) || 
                            startsWith(bytes, zipSignature3);
        
        System.out.println("Excel file validation - ZIP signature: " + isValidZip);
        return isValidZip;
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
     * Extrae los datos del paciente desde la hoja de Excel
     */
    private void extractPatientData(Sheet sheet, PatientProfile patient) {
        try {
            System.out.println("=== EXTRAYENDO DATOS DEL PACIENTE ===");
            
            // Primero, vamos a explorar el contenido de la hoja
            System.out.println("Explorando contenido de la hoja...");
            for (int rowIndex = 0; rowIndex <= Math.min(20, sheet.getLastRowNum()); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    System.out.println("Fila " + rowIndex + ":");
                    for (int colIndex = 0; colIndex < Math.min(10, row.getLastCellNum()); colIndex++) {
                        Cell cell = row.getCell(colIndex);
                        if (cell != null) {
                            String cellValue = getCellValueAsString(cell);
                            if (cellValue != null && !cellValue.trim().isEmpty()) {
                                System.out.println("  Col " + colIndex + ": " + cellValue);
                            }
                        }
                    }
                }
            }
            
            // Buscar datos del paciente en las celdas
            // Basado en el formulario médico mostrado
            
            // Datos personales del paciente
            String paternalSurname = getCellValueAsString(sheet, "Apellido Paterno");
            System.out.println("Apellido Paterno encontrado: " + paternalSurname);
            patient.setPaternalSurname(paternalSurname);
            
            String maternalSurname = getCellValueAsString(sheet, "Apellido Materno");
            System.out.println("Apellido Materno encontrado: " + maternalSurname);
            patient.setMaternalSurname(maternalSurname);
            
            String firstNames = getCellValueAsString(sheet, "Nombres Completos");
            System.out.println("Nombres Completos encontrados: " + firstNames);
            patient.setFirstNames(firstNames);
            
            String birthPlace = getCellValueAsString(sheet, "Lugar de Nacimiento");
            System.out.println("Lugar de Nacimiento encontrado: " + birthPlace);
            patient.setBirthPlace(birthPlace);
            
            // Fecha de nacimiento
            String birthDateStr = getCellValueAsString(sheet, "Fecha de Nacimiento");
            System.out.println("Fecha de Nacimiento encontrada: " + birthDateStr);
            if (birthDateStr != null && !birthDateStr.isEmpty()) {
                patient.setBirthDate(parseDate(birthDateStr));
            }
            
            String gender = getCellValueAsString(sheet, "Sexo");
            System.out.println("Sexo encontrado: " + gender);
            patient.setGender(gender);
            
            String maritalStatus = getCellValueAsString(sheet, "Estado Civil");
            System.out.println("Estado Civil encontrado: " + maritalStatus);
            patient.setMaritalStatus(maritalStatus);
            
            String currentAddress = getCellValueAsString(sheet, "Domicilio Actual");
            System.out.println("Domicilio Actual encontrado: " + currentAddress);
            patient.setCurrentAddress(currentAddress);
            
            String district = getCellValueAsString(sheet, "Distrito/Provincia/Región o Estado/País");
            System.out.println("Distrito encontrado: " + district);
            patient.setDistrict(district);
            
            String identityDocument = getCellValueAsString(sheet, "Documento de Identidad");
            System.out.println("Documento de Identidad encontrado: " + identityDocument);
            patient.setIdentityDocumentNumber(identityDocument);
            
            String phone = getCellValueAsString(sheet, "Fijo Casa/Celular");
            System.out.println("Teléfono encontrado: " + phone);
            patient.setPhone(phone);
            
            String email = getCellValueAsString(sheet, "Correo electrónico");
            System.out.println("Email encontrado: " + email);
            patient.setEmail(email);
            
            String educationLevel = getCellValueAsString(sheet, "Grado de Instrucción");
            System.out.println("Grado de Instrucción encontrado: " + educationLevel);
            patient.setEducationLevel(educationLevel);
            
            String occupation = getCellValueAsString(sheet, "Ocupación");
            System.out.println("Ocupación encontrada: " + occupation);
            patient.setOccupation(occupation);
            
            String currentEducationalInstitution = getCellValueAsString(sheet, "Institución Educativa Actual");
            System.out.println("Institución Educativa Actual encontrada: " + currentEducationalInstitution);
            patient.setCurrentEducationalInstitution(currentEducationalInstitution);
            
            String religion = getCellValueAsString(sheet, "Religión");
            System.out.println("Religión encontrada: " + religion);
            patient.setReligion(religion);
            
            // Calcular edad (simplificado)
            if (patient.getBirthDate() != null) {
                LocalDate now = LocalDate.now();
                int age = now.getYear() - patient.getBirthDate().getYear();
                patient.setAgeCurrent(age);
                System.out.println("Edad calculada: " + age);
            }
            
            System.out.println("=== EXTRACCIÓN COMPLETADA ===");
            
        } catch (Exception e) {
            System.err.println("Error extracting patient data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Busca el valor de una celda basado en el texto de la etiqueta
     */
    private String getCellValueAsString(Sheet sheet, String labelText) {
        try {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.STRING) {
                        String cellValue = cell.getStringCellValue().trim();
                        if (cellValue.contains(labelText)) {
                            // Buscar la celda adyacente con el valor
                            int columnIndex = cell.getColumnIndex();
                            int rowIndex = cell.getRowIndex();
                            
                            // Buscar en la misma fila, columna siguiente
                            Cell valueCell = row.getCell(columnIndex + 1);
                            if (valueCell != null) {
                                return getCellValueAsString(valueCell);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error searching for label: " + labelText + " - " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Convierte una celda a String independientemente de su tipo
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * Parsea una fecha desde String
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        try {
            // Intentar diferentes formatos de fecha
            String[] formats = {
                "M/d/yyyy", "MM/dd/yyyy", "d/M/yyyy", "dd/MM/yyyy",
                "yyyy-MM-dd", "dd-MM-yyyy", "MM-dd-yyyy"
            };
            
            for (String format : formats) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    return LocalDate.parse(dateStr, formatter);
                } catch (DateTimeParseException e) {
                    // Continuar con el siguiente formato
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing date: " + dateStr + " - " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Crea un paciente por defecto en caso de error
     */
    private PatientProfile createDefaultPatient(String errorMessage) {
        PatientProfile patient = new PatientProfile();
        patient.setFirstNames("Error al parsear");
        patient.setPaternalSurname("Datos");
        patient.setMaternalSurname("No disponibles");
        patient.setIdentityDocumentNumber("N/A");
        patient.setEmail("error@example.com");
        patient.setPhone("N/A");
        patient.setCurrentAddress("Error: " + errorMessage);
        return patient;
    }
    
    /**
     * Crea un paciente por defecto en caso de error (método legacy)
     */
    private PatientProfile createDefaultPatient() {
        return createDefaultPatient("Unknown error");
    }
    
    /**
     * Intenta reparar un archivo ZIP corrupto
     * @param corruptedBytes Bytes del archivo ZIP corrupto
     * @return Bytes reparados o null si no se puede reparar
     */
    private byte[] repairZipFile(byte[] corruptedBytes) {
        try {
            System.out.println("=== INICIANDO REPARACIÓN DE ZIP ===");
            System.out.println("Bytes originales: " + corruptedBytes.length);
            
            ByteArrayOutputStream repairedOutput = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(repairedOutput);
            
            try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(corruptedBytes))) {
                ZipEntry entry;
                while ((entry = zipIn.getNextEntry()) != null) {
                    try {
                        // Crear nueva entrada
                        ZipEntry newEntry = new ZipEntry(entry.getName());
                        zipOut.putNextEntry(newEntry);
                        
                        // Copiar datos de la entrada
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zipIn.read(buffer)) > 0) {
                            zipOut.write(buffer, 0, length);
                        }
                        
                        zipOut.closeEntry();
                        zipIn.closeEntry();
                        
                        System.out.println("Entrada reparada: " + entry.getName());
                        
                    } catch (Exception e) {
                        System.err.println("Error reparando entrada " + entry.getName() + ": " + e.getMessage());
                        // Continuar con la siguiente entrada
                    }
                }
            }
            
            zipOut.close();
            byte[] repairedBytes = repairedOutput.toByteArray();
            
            System.out.println("ZIP reparado exitosamente");
            System.out.println("Bytes reparados: " + repairedBytes.length);
            
            return repairedBytes;
            
        } catch (Exception e) {
            System.err.println("Error en reparación de ZIP: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Valida y limpia el base64 de forma más robusta
     * @param base64Content Contenido base64 a validar
     * @return Base64 limpio y validado
     */
    private String robustBase64Validation(String base64Content) {
        if (base64Content == null || base64Content.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 content is null or empty");
        }
        
        System.out.println("=== VALIDACIÓN ROBUSTA DE BASE64 ===");
        System.out.println("Longitud original: " + base64Content.length());
        
        // Limpiar caracteres no válidos
        String cleaned = base64Content.replaceAll("\\s+", "")
                                    .replaceAll("[^A-Za-z0-9+/=]", "");
        
        // Verificar longitud mínima
        if (cleaned.length() < 100) {
            throw new IllegalArgumentException("Base64 demasiado corto: " + cleaned.length() + " caracteres");
        }
        
        // Verificar formato base64
        if (!cleaned.matches("^[A-Za-z0-9+/]*={0,2}$")) {
            throw new IllegalArgumentException("Base64 contiene caracteres inválidos");
        }
        
        // Ajustar padding
        int remainder = cleaned.length() % 4;
        if (remainder != 0) {
            int paddingNeeded = 4 - remainder;
            cleaned += "====".substring(0, paddingNeeded);
            System.out.println("Padding agregado: " + paddingNeeded + " caracteres");
        }
        
        // Validar que se puede decodificar
        try {
            byte[] testDecode = Base64.getDecoder().decode(cleaned);
            System.out.println("Base64 válido - Bytes decodificados: " + testDecode.length);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Base64 no se puede decodificar: " + e.getMessage());
        }
        
        System.out.println("=== VALIDACIÓN ROBUSTA COMPLETADA ===");
        return cleaned;
    }
}
