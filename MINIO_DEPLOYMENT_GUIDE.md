# MinIO Configuration and Deployment Guide

## Descripción
MinIO es un servidor de object storage compatible con Amazon S3. Se usa para almacenar archivos Excel de forma segura y eficiente, evitando el envío de archivos grandes a través de ActiveMQ.

## Instalación de MinIO

### Opción 1: Docker (Recomendado)
```bash
# Crear directorio para datos
mkdir -p ~/minio-data

# Ejecutar MinIO con Docker
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=admin" \
  -e "MINIO_ROOT_PASSWORD=admin12345" \
  -v ~/minio-data:/data \
  quay.io/minio/minio server /data --console-address ":9001"
```

### Opción 2: Binario Directo
```bash
# Descargar MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio
chmod +x minio

# Ejecutar MinIO
export MINIO_ROOT_USER=admin
export MINIO_ROOT_PASSWORD=admin12345
./minio server ~/minio-data --console-address ":9001"
```

### Opción 3: Windows
```powershell
# Descargar MinIO para Windows
Invoke-WebRequest -Uri "https://dl.min.io/server/minio/release/windows-amd64/minio.exe" -OutFile "minio.exe"

# Ejecutar MinIO
$env:MINIO_ROOT_USER="admin"
$env:MINIO_ROOT_PASSWORD="admin12345"
.\minio.exe server C:\minio-data --console-address ":9001"
```

## Configuración Inicial

### 1. Acceder a MinIO Console
- **URL:** http://localhost:9001
- **Usuario:** admin
- **Contraseña:** admin12345

### 2. Crear Bucket
1. Ir a **Buckets** → **Create Bucket**
2. **Bucket Name:** `my-bucket`
3. **Region:** `us-east-1`
4. **Access Policy:** Private (recomendado)

### 3. Configurar Access Policy (Opcional)
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": "arn:aws:s3:::my-bucket/*"
    }
  ]
}
```

## Configuración del Sistema

### Variables de Entorno
```bash
# MinIO Configuration
S3_ENDPOINT=http://localhost:9000
S3_REGION=us-east-1
S3_ACCESS_KEY=admin
S3_SECRET_KEY=admin12345
S3_BUCKET=my-bucket
S3_FORCE_PATH_STYLE=true
```

### API Gateway (.env)
```env
# MinIO Configuration
S3_ENDPOINT=http://localhost:9000
S3_REGION=us-east-1
S3_ACCESS_KEY=admin
S3_SECRET_KEY=admin12345
S3_BUCKET=my-bucket
S3_FORCE_PATH_STYLE=true
```

### DocExcelParser (WildFly)
Agregar en `standalone.xml`:
```xml
<system-properties>
    <property name="S3_ENDPOINT" value="http://localhost:9000"/>
    <property name="S3_REGION" value="us-east-1"/>
    <property name="S3_ACCESS_KEY" value="admin"/>
    <property name="S3_SECRET_KEY" value="admin12345"/>
    <property name="S3_BUCKET" value="my-bucket"/>
    <property name="S3_FORCE_PATH_STYLE" value="true"/>
</system-properties>
```

## Verificación de Configuración

### 1. Health Check API Gateway
```bash
curl http://localhost:3001/api/minio/health
```

**Respuesta esperada:**
```json
{
  "status": "ok",
  "message": "MinIO connection successful",
  "endpoint": "http://localhost:9000",
  "bucket": "my-bucket",
  "timestamp": "2025-10-22T12:00:00.000Z"
}
```

### 2. Probar Subida de Archivo
```bash
curl -X POST http://localhost:3001/api/excel/upload-and-process \
  -F "file=@test.xlsx"
```

### 3. Verificar en MinIO Console
- Ir a **Buckets** → **my-bucket**
- Verificar que el archivo se subió correctamente

## Operaciones MinIO

### Subir Archivo
```javascript
const AWS = require('aws-sdk');

const s3 = new AWS.S3({
    endpoint: 'http://localhost:9000',
    accessKeyId: 'admin',
    secretAccessKey: 'admin12345',
    region: 'us-east-1',
    s3ForcePathStyle: true
});

const uploadParams = {
    Bucket: 'my-bucket',
    Key: 'uploads/test.xlsx',
    Body: fileBuffer,
    ContentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
};

const result = await s3.upload(uploadParams).promise();
```

### Descargar Archivo
```javascript
const downloadParams = {
    Bucket: 'my-bucket',
    Key: 'uploads/test.xlsx'
};

const data = await s3.getObject(downloadParams).promise();
const fileContent = data.Body;
```

### Generar Presigned URL
```javascript
const presignedParams = {
    Bucket: 'my-bucket',
    Key: 'uploads/test.xlsx',
    Expires: 900 // 15 minutos
};

const presignedUrl = s3.getSignedUrl('getObject', presignedParams);
```

## Configuración de Producción

### Variables de Entorno Seguras
```bash
# Usar variables de entorno en lugar de hardcoded
export MINIO_ROOT_USER="${MINIO_USER}"
export MINIO_ROOT_PASSWORD="${MINIO_PASSWORD}"

# Configurar endpoint personalizado
export S3_ENDPOINT="${MINIO_ENDPOINT}"
export S3_ACCESS_KEY="${MINIO_ACCESS_KEY}"
export S3_SECRET_KEY="${MINIO_SECRET_KEY}"
```

### Configuración de Red
```bash
# MinIO con configuración de red personalizada
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  --network custom-network \
  -e "MINIO_ROOT_USER=${MINIO_USER}" \
  -e "MINIO_ROOT_PASSWORD=${MINIO_PASSWORD}" \
  -v ~/minio-data:/data \
  quay.io/minio/minio server /data --console-address ":9001"
```

### Configuración de SSL/TLS
```bash
# MinIO con SSL
docker run -d \
  --name minio \
  -p 443:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=admin" \
  -e "MINIO_ROOT_PASSWORD=admin12345" \
  -v ~/minio-data:/data \
  -v ~/certs:/root/.minio/certs \
  quay.io/minio/minio server /data --console-address ":9001"
```

## Monitoreo y Logs

### Logs de MinIO
```bash
# Ver logs de MinIO (Docker)
docker logs minio

# Ver logs en tiempo real
docker logs -f minio
```

### Métricas de MinIO
- **Console:** http://localhost:9001 → **Monitoring**
- **Prometheus:** http://localhost:9000/minio/v2/metrics/cluster

### Health Check
```bash
# Health check básico
curl http://localhost:9000/minio/health/live

# Health check detallado
curl http://localhost:9000/minio/health/ready
```

## Troubleshooting

### Error: "Connection refused"
**Causa:** MinIO no está corriendo
**Solución:** 
```bash
# Verificar que MinIO esté corriendo
docker ps | grep minio

# Iniciar MinIO si no está corriendo
docker start minio
```

### Error: "Access Denied"
**Causa:** Credenciales incorrectas
**Solución:** Verificar S3_ACCESS_KEY y S3_SECRET_KEY

### Error: "Bucket does not exist"
**Causa:** Bucket no creado
**Solución:** Crear bucket `my-bucket` en MinIO Console

### Error: "Invalid endpoint"
**Causa:** S3_ENDPOINT mal configurado
**Solución:** Verificar que S3_ENDPOINT sea `http://localhost:9000`

## Backup y Restauración

### Backup de Datos
```bash
# Backup completo del directorio de datos
tar -czf minio-backup-$(date +%Y%m%d).tar.gz ~/minio-data/

# Backup específico de bucket
mc mirror my-bucket/ ~/backup/my-bucket/
```

### Restauración
```bash
# Restaurar desde backup
tar -xzf minio-backup-20251022.tar.gz -C ~/

# Restaurar bucket específico
mc mirror ~/backup/my-bucket/ my-bucket/
```

## Seguridad

### Configuración de Usuarios
```bash
# Crear usuario adicional
mc admin user add myminio newuser newpassword

# Crear política personalizada
mc admin policy add myminio readwrite-policy /path/to/policy.json

# Asignar política a usuario
mc admin policy set myminio readwrite-policy user=newuser
```

### Configuración de CORS
```json
{
  "CORSRules": [
    {
      "AllowedOrigins": ["http://localhost:3001"],
      "AllowedMethods": ["GET", "PUT", "POST"],
      "AllowedHeaders": ["*"],
      "MaxAgeSeconds": 3000
    }
  ]
}
```

## Estado del Proyecto

✅ **Instalación:** Documentada  
✅ **Configuración:** Documentada  
✅ **Integración API Gateway:** Implementada  
✅ **Integración DocExcelParser:** Implementada  
✅ **Health Checks:** Implementados  
✅ **Troubleshooting:** Documentado  
✅ **Seguridad:** Documentada  
✅ **Backup/Restore:** Documentado
