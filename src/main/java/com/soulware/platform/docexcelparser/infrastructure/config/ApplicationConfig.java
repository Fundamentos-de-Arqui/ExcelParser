package com.soulware.platform.docexcelparser.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Clase de configuración centralizada que lee valores desde variables de entorno
 * con valores por defecto para desarrollo local
 */
@ApplicationScoped
public class ApplicationConfig {
    
    private static final Logger logger = Logger.getLogger(ApplicationConfig.class.getName());
    
    // ========== JMS/ActiveMQ Configuration ==========
    
    /**
     * URL del broker JMS (ActiveMQ)
     * Variable de entorno: JMS_BROKER_URL
     * Default: tcp://localhost:61616
     */
    public String getJmsBrokerUrl() {
        return System.getenv("JMS_BROKER_URL") != null ? 
            System.getenv("JMS_BROKER_URL") : "tcp://localhost:61616";
    }
    
    /**
     * URL HTTP del broker ActiveMQ para API REST
     * Variable de entorno: JMS_BROKER_HTTP_URL
     * Default: http://localhost:8161/api/message
     */
    public String getJmsBrokerHttpUrl() {
        return System.getenv("JMS_BROKER_HTTP_URL") != null ? 
            System.getenv("JMS_BROKER_HTTP_URL") : "http://localhost:8161/api/message";
    }
    
    /**
     * URL Jolokia del broker ActiveMQ
     * Variable de entorno: JMS_BROKER_JOLOKIA_URL
     * Default: http://localhost:8161/api/jolokia
     */
    public String getJmsBrokerJolokiaUrl() {
        return System.getenv("JMS_BROKER_JOLOKIA_URL") != null ? 
            System.getenv("JMS_BROKER_JOLOKIA_URL") : "http://localhost:8161/api/jolokia";
    }
    
    /**
     * Nombre de la cola principal de entrada de Excel
     * Variable de entorno: JMS_QUEUE_EXCEL_INPUT
     * Default: excel-input-queue
     */
    public String getJmsQueueExcelInput() {
        return System.getenv("JMS_QUEUE_EXCEL_INPUT") != null ? 
            System.getenv("JMS_QUEUE_EXCEL_INPUT") : "excel-input-queue";
    }
    
    /**
     * Nombre de la cola para datos de pacientes
     * Variable de entorno: JMS_QUEUE_PATIENT_DATA
     * Default: patient-data-queue
     */
    public String getJmsQueuePatientData() {
        return System.getenv("JMS_QUEUE_PATIENT_DATA") != null ? 
            System.getenv("JMS_QUEUE_PATIENT_DATA") : "patient-data-queue";
    }
    
    /**
     * Nombre de la cola para formularios de pacientes
     * Variable de entorno: JMS_QUEUE_PATIENT_FORM
     * Default: excelParser_patientForm
     */
    public String getJmsQueuePatientForm() {
        return System.getenv("JMS_QUEUE_PATIENT_FORM") != null ? 
            System.getenv("JMS_QUEUE_PATIENT_FORM") : "excelParser_patientForm";
    }
    
    /**
     * Nombre de la cola para links de Excel generados
     * Variable de entorno: JMS_QUEUE_EXCEL_GENERATED_LINKS
     * Default: excel-generated-links
     */
    public String getJmsQueueExcelGeneratedLinks() {
        return System.getenv("JMS_QUEUE_EXCEL_GENERATED_LINKS") != null ? 
            System.getenv("JMS_QUEUE_EXCEL_GENERATED_LINKS") : "excel-generated-links";
    }
    
    /**
     * Nombre de la cola alternativa (legacy)
     * Variable de entorno: JMS_QUEUE_EXCEL_INPUT_ALT
     * Default: excel.input.queue
     */
    public String getJmsQueueExcelInputAlt() {
        return System.getenv("JMS_QUEUE_EXCEL_INPUT_ALT") != null ? 
            System.getenv("JMS_QUEUE_EXCEL_INPUT_ALT") : "excel.input.queue";
    }
    
    // ========== MinIO/S3 Configuration ==========
    
    /**
     * Endpoint de S3/MinIO
     * Variable de entorno: S3_ENDPOINT
     * Default: https://ecjzscyihpidhjbkuimh.storage.supabase.co/storage/v1/s3
     */
    public String getS3Endpoint() {
        return System.getenv("S3_ENDPOINT") != null ? 
            System.getenv("S3_ENDPOINT") : "https://ecjzscyihpidhjbkuimh.storage.supabase.co/storage/v1/s3";
    }
    
    /**
     * Nombre del bucket S3/MinIO
     * Variable de entorno: S3_BUCKET
     * Default: my-bucket
     */
    public String getS3Bucket() {
        return System.getenv("S3_BUCKET") != null ? 
            System.getenv("S3_BUCKET") : "my-bucket";
    }
    
    /**
     * Access Key de S3/MinIO
     * Variable de entorno: S3_ACCESS_KEY
     * Default: b4fc0906c69779eaee5e9db979daf993
     */
    public String getS3AccessKey() {
        return System.getenv("S3_ACCESS_KEY") != null ? 
            System.getenv("S3_ACCESS_KEY") : "b4fc0906c69779eaee5e9db979daf993";
    }
    
    /**
     * Secret Key de S3/MinIO
     * Variable de entorno: S3_SECRET_KEY
     * Default: (valor por defecto)
     */
    public String getS3SecretKey() {
        return System.getenv("S3_SECRET_KEY") != null ? 
            System.getenv("S3_SECRET_KEY") : "6c4a20528c4c02973df8089dc38c39985326aafeac242c57c79e753c543bc8ec";
    }
    
    /**
     * Región de S3/MinIO
     * Variable de entorno: S3_REGION
     * Default: us-east-1
     */
    public String getS3Region() {
        return System.getenv("S3_REGION") != null ? 
            System.getenv("S3_REGION") : "us-east-1";
    }
    
    // ========== Server Configuration ==========
    
    /**
     * Puerto del servidor de aplicaciones (WildFly)
     * Variable de entorno: SERVER_PORT
     * Default: 8080
     */
    public int getServerPort() {
        String portStr = System.getenv("SERVER_PORT");
        if (portStr != null) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                logger.warning("Invalid SERVER_PORT value: " + portStr + ", using default 8080");
            }
        }
        return 8080;
    }
    
    /**
     * Host del servidor de aplicaciones
     * Variable de entorno: SERVER_HOST
     * Default: localhost
     */
    public String getServerHost() {
        return System.getenv("SERVER_HOST") != null ? 
            System.getenv("SERVER_HOST") : "localhost";
    }
    
    /**
     * Imprime la configuración actual (sin valores sensibles completos)
     */
    public void printConfiguration() {
        logger.info("=== APPLICATION CONFIGURATION ===");
        logger.info("JMS Broker URL: " + getJmsBrokerUrl());
        logger.info("JMS Broker HTTP URL: " + getJmsBrokerHttpUrl());
        logger.info("JMS Queue Excel Input: " + getJmsQueueExcelInput());
        logger.info("JMS Queue Patient Data: " + getJmsQueuePatientData());
        logger.info("JMS Queue Patient Form: " + getJmsQueuePatientForm());
        logger.info("JMS Queue Excel Generated Links: " + getJmsQueueExcelGeneratedLinks());
        logger.info("S3 Endpoint: " + getS3Endpoint());
        logger.info("S3 Bucket: " + getS3Bucket());
        logger.info("S3 Region: " + getS3Region());
        logger.info("Server Host: " + getServerHost());
        logger.info("Server Port: " + getServerPort());
        logger.info("=================================");
    }
}

