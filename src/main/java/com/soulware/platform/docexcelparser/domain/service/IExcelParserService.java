package com.soulware.platform.docexcelparser.domain.service;

import com.soulware.platform.docexcelparser.domain.model.PatientProfile;

/**
 * Interfaz de servicio de dominio para parsing de archivos Excel
 * Define el contrato para extraer datos de pacientes desde Excel
 */
public interface IExcelParserService {
    
    /**
     * Parsea un archivo Excel desde base64 y extrae los datos del paciente
     * @param base64Content Contenido base64 del archivo Excel
     * @return PatientProfile con los datos extraídos
     */
    PatientProfile parsePatientFromExcel(String base64Content);
}

