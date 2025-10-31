package com.soulware.platform.docexcelparser.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Servicio para convertir formularios de pacientes desde JSON a Excel
 */
public class PatientFormToExcelService {
    
    private static final Logger logger = Logger.getLogger(PatientFormToExcelService.class.getName());
    
    /**
     * Convierte un formulario de paciente desde JSON a Excel
     * @param patientJson JSON del formulario de paciente
     * @return Bytes del archivo Excel generado
     * @throws IOException Si hay error en la generación
     */
    public byte[] convertPatientFormToExcel(JsonNode patientJson) throws IOException {
        logger.info("=== CONVIRTIENDO FORMULARIO DE PACIENTE A EXCEL ===");
        
        // Crear nuevo workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Formulario de Paciente");
        
        // Crear estilos
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Título principal
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("FORMULARIO DE IDENTIFICACIÓN DEL PACIENTE");
        titleCell.setCellStyle(headerStyle);
        
        // Fusionar celdas para el título
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));
        
        rowNum++; // Línea en blanco
        
        // Información básica del paciente
        rowNum = addPatientBasicInfo(sheet, patientJson, rowNum, headerStyle, dataStyle);
        
        rowNum++; // Línea en blanco
        
        // Información de contacto
        rowNum = addContactInfo(sheet, patientJson, rowNum, headerStyle, dataStyle);
        
        rowNum++; // Línea en blanco
        
        // Información adicional
        rowNum = addAdditionalInfo(sheet, patientJson, rowNum, headerStyle, dataStyle);
        
        rowNum++; // Línea en blanco
        
        // Responsable legal
        if (patientJson.has("legalResponsible")) {
            rowNum = addLegalResponsibleInfo(sheet, patientJson.get("legalResponsible"), rowNum, headerStyle, dataStyle);
            rowNum++; // Línea en blanco
        }
        
        // Terapeuta
        if (patientJson.has("therapist")) {
            rowNum = addTherapistInfo(sheet, patientJson.get("therapist"), rowNum, headerStyle, dataStyle);
        }
        
        // Ajustar ancho de columnas
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        
        // Convertir a bytes
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        byte[] excelBytes = outputStream.toByteArray();
        outputStream.close();
        
        logger.info("✅ Excel generado exitosamente: " + excelBytes.length + " bytes");
        return excelBytes;
    }
    
    /**
     * Agrega información básica del paciente
     */
    private int addPatientBasicInfo(Sheet sheet, JsonNode patientJson, int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = startRow;
        
        // Encabezado de sección
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("INFORMACIÓN BÁSICA");
        sectionCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 1));
        
        // Datos básicos
        addDataRow(sheet, rowNum++, "Nombres:", getStringValue(patientJson, "firstNames"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Apellido Paterno:", getStringValue(patientJson, "paternalSurname"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Apellido Materno:", getStringValue(patientJson, "maternalSurname"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Fecha de Nacimiento:", getStringValue(patientJson, "birthDate"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Lugar de Nacimiento:", getStringValue(patientJson, "birthPlace"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Edad Actual:", getStringValue(patientJson, "currentAge"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Género:", getStringValue(patientJson, "gender"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Tipo de Documento:", getStringValue(patientJson, "documentType"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Número de Documento:", getStringValue(patientJson, "identityDocumentNumber"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Estado Civil:", getStringValue(patientJson, "maritalStatus"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Ocupación:", getStringValue(patientJson, "occupation"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Nivel de Educación:", getStringValue(patientJson, "educationLevel"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Institución Educativa:", getStringValue(patientJson, "currentEducationalInstitution"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Religión:", getStringValue(patientJson, "religion"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Edad Primera Consulta:", getStringValue(patientJson, "firstAppointmentAge"), headerStyle, dataStyle);
        
        return rowNum;
    }
    
    /**
     * Agrega información de contacto
     */
    private int addContactInfo(Sheet sheet, JsonNode patientJson, int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = startRow;
        
        // Encabezado de sección
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("INFORMACIÓN DE CONTACTO");
        sectionCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 1));
        
        // Datos de contacto
        addDataRow(sheet, rowNum++, "Email:", getStringValue(patientJson, "email"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Teléfono:", getStringValue(patientJson, "phone"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Dirección:", getStringValue(patientJson, "currentAddress"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Distrito:", getStringValue(patientJson, "district"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Provincia:", getStringValue(patientJson, "province"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Región:", getStringValue(patientJson, "region"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "País:", getStringValue(patientJson, "country"), headerStyle, dataStyle);
        
        return rowNum;
    }
    
    /**
     * Agrega información adicional
     */
    private int addAdditionalInfo(Sheet sheet, JsonNode patientJson, int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = startRow;
        
        // Encabezado de sección
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("INFORMACIÓN ADICIONAL");
        sectionCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 1));
        
        // Datos adicionales
        addDataRow(sheet, rowNum++, "Médico Tratante:", getStringValue(patientJson, "referredTherapistName"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "ID del Paciente:", getStringValue(patientJson, "id"), headerStyle, dataStyle);
        
        return rowNum;
    }
    
    /**
     * Agrega información del responsable legal
     */
    private int addLegalResponsibleInfo(Sheet sheet, JsonNode legalResponsible, int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = startRow;
        
        // Encabezado de sección
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("RESPONSABLE LEGAL");
        sectionCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 1));
        
        // Datos del responsable legal
        addDataRow(sheet, rowNum++, "Nombres:", getStringValue(legalResponsible, "firstNames"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Apellido Paterno:", getStringValue(legalResponsible, "paternalSurname"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Apellido Materno:", getStringValue(legalResponsible, "maternalSurname"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Tipo de Documento:", getStringValue(legalResponsible, "documentType"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Número de Documento:", getStringValue(legalResponsible, "identityDocumentNumber"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Teléfono:", getStringValue(legalResponsible, "phone"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Email:", getStringValue(legalResponsible, "email"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Parentesco:", getStringValue(legalResponsible, "relationship"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "ID:", getStringValue(legalResponsible, "id"), headerStyle, dataStyle);
        
        return rowNum;
    }
    
    /**
     * Agrega información del terapeuta
     */
    private int addTherapistInfo(Sheet sheet, JsonNode therapist, int startRow, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = startRow;
        
        // Encabezado de sección
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("TERAPEUTA");
        sectionCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 1));
        
        // Datos del terapeuta
        addDataRow(sheet, rowNum++, "Nombres:", getStringValue(therapist, "firstNames"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Apellido Paterno:", getStringValue(therapist, "paternalSurname"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Apellido Materno:", getStringValue(therapist, "maternalSurname"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Tipo de Documento:", getStringValue(therapist, "documentType"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Número de Documento:", getStringValue(therapist, "identityDocumentNumber"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Teléfono:", getStringValue(therapist, "phone"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Email:", getStringValue(therapist, "email"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Especialidad:", getStringValue(therapist, "specialtyName"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "Dirección de Atención:", getStringValue(therapist, "attentionPlaceAddress"), headerStyle, dataStyle);
        addDataRow(sheet, rowNum++, "ID:", getStringValue(therapist, "id"), headerStyle, dataStyle);
        
        return rowNum;
    }
    
    /**
     * Agrega una fila de datos al Excel
     */
    private void addDataRow(Sheet sheet, int rowNum, String label, String value, CellStyle headerStyle, CellStyle dataStyle) {
        Row row = sheet.createRow(rowNum);
        
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value != null ? value : "");
        valueCell.setCellStyle(dataStyle);
    }
    
    /**
     * Obtiene un valor string del JSON de forma segura
     */
    private String getStringValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return "";
    }
    
    /**
     * Crea estilo para encabezados
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Crea estilo para datos
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
}
