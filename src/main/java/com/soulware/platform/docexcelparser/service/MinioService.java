package com.soulware.platform.docexcelparser.service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Servicio para interactuar con MinIO (compatible con S3)
 */
public class MinioService {
    
    private static final Logger logger = Logger.getLogger(MinioService.class.getName());
    
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String endpoint;
    
    public MinioService() {
        // Configuración desde variables de entorno o valores por defecto
        this.endpoint = System.getenv("S3_ENDPOINT") != null ? 
            System.getenv("S3_ENDPOINT") : "http://localhost:9000";
        this.bucketName = System.getenv("S3_BUCKET") != null ? 
            System.getenv("S3_BUCKET") : "my-bucket";
        
        String accessKey = System.getenv("S3_ACCESS_KEY") != null ? 
            System.getenv("S3_ACCESS_KEY") : "admin";
        String secretKey = System.getenv("S3_SECRET_KEY") != null ? 
            System.getenv("S3_SECRET_KEY") : "admin12345";
        String region = System.getenv("S3_REGION") != null ? 
            System.getenv("S3_REGION") : "us-east-1";
        
        // Configurar credenciales
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        
        // Configurar cliente S3 para MinIO
        this.s3Client = S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true) // Importante para MinIO
                .build())
            .build();
            
        // Configurar presigner
        this.s3Presigner = S3Presigner.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
            .serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build())
            .build();
        
        logger.info("🗂️  MinIO Service initialized:");
        logger.info("   Endpoint: " + endpoint);
        logger.info("   Bucket: " + bucketName);
        logger.info("   Region: " + region);
    }
    
    /**
     * Descarga un archivo del bucket MinIO
     * @param key Clave del archivo
     * @return Bytes del archivo
     * @throws IOException Si hay error en la descarga
     */
    public byte[] downloadFile(String key) throws IOException {
        try {
            logger.info("📥 Downloading file: " + key);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest);
            byte[] fileBytes = response.readAllBytes();
            
            logger.info("✅ File downloaded successfully: " + key);
            logger.info("   Size: " + fileBytes.length + " bytes");
            
            return fileBytes;
        } catch (Exception e) {
            logger.severe("❌ Error downloading file " + key + ": " + e.getMessage());
            throw new IOException("Failed to download file " + key + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica si un archivo existe en el bucket
     * @param key Clave del archivo
     * @return true si existe
     */
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            s3Client.headObject(headObjectRequest);
            logger.info("✅ File exists: " + key);
            return true;
        } catch (NoSuchKeyException e) {
            logger.info("❌ File not found: " + key);
            return false;
        } catch (Exception e) {
            logger.severe("❌ Error checking file existence: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina un archivo del bucket
     * @param key Clave del archivo
     */
    public void deleteFile(String key) {
        try {
            logger.info("🗑️  Deleting file: " + key);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("✅ File deleted successfully: " + key);
        } catch (Exception e) {
            logger.severe("❌ Error deleting file " + key + ": " + e.getMessage());
        }
    }
    
    /**
     * Obtiene información de un archivo
     * @param key Clave del archivo
     * @return Información del archivo
     */
    public FileInfo getFileInfo(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            
            return new FileInfo(
                key,
                response.contentLength(),
                response.contentType(),
                response.lastModified(),
                response.eTag()
            );
        } catch (Exception e) {
            logger.severe("❌ Error getting file info: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera una presigned URL para descargar un archivo
     * @param key Clave del archivo
     * @param expiresInMinutes Minutos hasta expiración
     * @return Presigned URL
     */
    public String generatePresignedGetUrl(String key, int expiresInMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
                
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiresInMinutes))
                .getObjectRequest(getObjectRequest)
                .build();
                
            String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();
            logger.info("✅ Presigned GET URL generated for: " + key);
            return presignedUrl;
        } catch (Exception e) {
            logger.severe("❌ Error generating presigned GET URL: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verifica la conexión con MinIO
     * @return true si la conexión es exitosa
     */
    public boolean testConnection() {
        try {
            logger.info("🔍 Testing MinIO connection...");
            
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
                
            s3Client.headBucket(headBucketRequest);
            logger.info("✅ MinIO connection successful");
            return true;
        } catch (Exception e) {
            logger.severe("❌ MinIO connection failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cierra las conexiones
     */
    public void close() {
        try {
            s3Client.close();
            s3Presigner.close();
            logger.info("✅ MinIO connections closed");
        } catch (Exception e) {
            logger.severe("❌ Error closing MinIO connections: " + e.getMessage());
        }
    }
    
    /**
     * Clase para información de archivo
     */
    public static class FileInfo {
        private final String key;
        private final long size;
        private final String contentType;
        private final java.time.Instant lastModified;
        private final String etag;
        
        public FileInfo(String key, long size, String contentType, java.time.Instant lastModified, String etag) {
            this.key = key;
            this.size = size;
            this.contentType = contentType;
            this.lastModified = lastModified;
            this.etag = etag;
        }
        
        // Getters
        public String getKey() { return key; }
        public long getSize() { return size; }
        public String getContentType() { return contentType; }
        public java.time.Instant getLastModified() { return lastModified; }
        public String getEtag() { return etag; }
        
        @Override
        public String toString() {
            return "FileInfo{" +
                "key='" + key + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", lastModified=" + lastModified +
                ", etag='" + etag + '\'' +
                '}';
        }
    }
}
