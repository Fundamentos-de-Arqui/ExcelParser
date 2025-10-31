# Configuración mediante Variables de Entorno

Este proyecto ahora soporta configuración mediante variables de entorno. Todas las configuraciones tienen valores por defecto para desarrollo local.

## Variables de Entorno Disponibles

### JMS/ActiveMQ Configuration

```env
# URL del broker JMS (ActiveMQ)
JMS_BROKER_URL=tcp://localhost:61616

# URL HTTP del broker ActiveMQ para API REST
JMS_BROKER_HTTP_URL=http://localhost:8161/api/message

# URL Jolokia del broker ActiveMQ
JMS_BROKER_JOLOKIA_URL=http://localhost:8161/api/jolokia

# Nombres de colas JMS
JMS_QUEUE_EXCEL_INPUT=excel-input-queue
JMS_QUEUE_PATIENT_DATA=patient-data-queue
JMS_QUEUE_PATIENT_FORM=excelParser_patientForm
JMS_QUEUE_EXCEL_GENERATED_LINKS=excel-generated-links
JMS_QUEUE_EXCEL_INPUT_ALT=excel.input.queue
```

### MinIO/S3 Configuration

```env
# Endpoint de S3/MinIO
S3_ENDPOINT=https://ecjzscyihpidhjbkuimh.storage.supabase.co/storage/v1/s3

# Nombre del bucket S3/MinIO
S3_BUCKET=my-bucket

# Access Key de S3/MinIO
S3_ACCESS_KEY=b4fc0906c69779eaee5e9db979daf993

# Secret Key de S3/MinIO
S3_SECRET_KEY=6c4a20528c4c02973df8089dc38c39985326aafeac242c57c79e753c543bc8ec

# Región de S3/MinIO
S3_REGION=us-east-1
```

### Server Configuration

```env
# Puerto del servidor de aplicaciones (WildFly)
SERVER_PORT=8080

# Host del servidor de aplicaciones
SERVER_HOST=localhost
```

## Uso

Para usar variables de entorno en producción, configúralas en tu servidor de aplicaciones (WildFly) o contenedor Docker antes de iniciar la aplicación.

### Ejemplo con WildFly

Edita `standalone.conf` o configura las variables de entorno del sistema operativo:

```bash
export JMS_BROKER_URL=tcp://activemq:61616
export S3_ENDPOINT=http://minio:9000
export S3_BUCKET=production-bucket
```

### Ejemplo con Docker

```yaml
environment:
  - JMS_BROKER_URL=tcp://activemq:61616
  - S3_ENDPOINT=http://minio:9000
  - S3_BUCKET=production-bucket
```

## Clase de Configuración

Todas las configuraciones se centralizan en:
- `com.soulware.platform.docexcelparser.infrastructure.config.ApplicationConfig`

Esta clase lee las variables de entorno y proporciona valores por defecto si no están configu Stilladas.

