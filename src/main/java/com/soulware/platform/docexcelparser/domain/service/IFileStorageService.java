package com.soulware.platform.docexcelparser.domain.service;

import java.io.IOException;

/**
 * Interfaz de servicio de dominio para almacenamiento de archivos
 * Define el contrato sin depender de implementaciones específicas (MinIO, S3, etc.)
 */
public interface IFileStorageService {
    
    /**
     * Descarga un archivo desde el almacenamiento
     * @param fileKey Clave del archivo
     * @return Bytes del archivo
     * @throws IOException Si hay error al descargar
     */
    byte[] downloadFile(String fileKey) throws IOException;
    
    /**
     * Verifica si un archivo existe
     * @param fileKey Clave del archivo
     * @return true si existe, false en caso contrario
     */
    boolean fileExists(String fileKey);
    
    /**
     * Obtiene información del archivo
     * @param fileKey Clave del archivo
     * @return Información del archivo
     */
    FileInfo getFileInfo(String fileKey);
    
    /**
     * Elimina un archivo
     * @param fileKey Clave del archivo
     * @throws IOException Si hay error al eliminar
     */
    void deleteFile(String fileKey) throws IOException;
    
    /**
     * Clase para información del archivo
     */
    class FileInfo {
        private final long size;
        private final String contentType;
        private final String lastModified;
        
        public FileInfo(long size, String contentType, String lastModified) {
            this.size = size;
            this.contentType = contentType;
            this.lastModified = lastModified;
        }
        
        public long getSize() {
            return size;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public String getLastModified() {
            return lastModified;
        }
    }
}

