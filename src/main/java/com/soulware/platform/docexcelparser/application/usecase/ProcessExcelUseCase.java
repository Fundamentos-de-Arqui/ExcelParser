package com.soulware.platform.docexcelparser.application.usecase;

import com.soulware.platform.docexcelparser.domain.model.PatientProfile;
import com.soulware.platform.docexcelparser.domain.service.IExcelParserService;
import com.soulware.platform.docexcelparser.domain.service.IFileStorageService;
import com.soulware.platform.docexcelparser.domain.service.IMessagePublisherService;
import com.soulware.platform.docexcelparser.domain.repository.IPatientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Base64;
import java.util.Optional;

/**
 * Caso de uso para procesar archivos Excel y extraer datos de pacientes
 */
@ApplicationScoped
@Named("processExcelUseCase")
public class ProcessExcelUseCase {

    @Inject
    private IExcelParserService excelParserService;
    
    @Inject
    private IFileStorageService fileStorageService;
    
    @Inject
    private IMessagePublisherService messagePublisherService;
    
    @Inject
    private IPatientRepository patientRepository;

    /**
     * Procesa un archivo Excel desde MinIO usando su fileKey
     * @param fileKey Clave del archivo en MinIO
     * @param fileName Nombre del archivo
     * @return PatientProfile procesado
     */
    public PatientProfile processExcelFromFileKey(String fileKey, String fileName) {
        try {
            // 1. Descargar archivo desde MinIO
            byte[] excelBytes = fileStorageService.downloadFile(fileKey);
            
            // 2. Convertir a base64 para el parser
            String base64Content = Base64.getEncoder().encodeToString(excelBytes);
            
            // 3. Parsear Excel
            PatientProfile patient = excelParserService.parsePatientFromExcel(base64Content);
            
            // 4. Guardar en repositorio
            patient = patientRepository.save(patient);
            
            // 5. Publicar mensaje con los datos procesados
            messagePublisherService.publishPatientData(patient);
            
            return patient;
            
        } catch (Exception e) {
            throw new RuntimeException("Error processing Excel from fileKey: " + fileKey, e);
        }
    }

    /**
     * Procesa un archivo Excel desde base64 directamente
     * @param base64Content Contenido base64 del Excel
     * @return PatientProfile procesado
     */
    public PatientProfile processExcelFromBase64(String base64Content) {
        try {
            // 1. Parsear Excel
            PatientProfile patient = excelParserService.parsePatientFromExcel(base64Content);
            
            // 2. Guardar en repositorio
            patient = patientRepository.save(patient);
            
            // 3. Publicar mensaje con los datos procesados
            messagePublisherService.publishPatientData(patient);
            
            return patient;
            
        } catch (Exception e) {
            throw new RuntimeException("Error processing Excel from base64", e);
        }
    }
}

