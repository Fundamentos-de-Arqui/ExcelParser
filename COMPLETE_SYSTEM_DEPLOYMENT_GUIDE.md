# Sistema Completo de Procesamiento de Excel - Guía de Deployment

## Descripción del Sistema
Sistema completo para procesar archivos Excel de formularios médicos con arquitectura distribuida que incluye:
- **API Gateway (Node.js):** Punto de entrada para subir archivos
- **MinIO:** Object storage para archivos Excel
- **ActiveMQ:** Message broker para comunicación asíncrona
- **DocExcelParser (WildFly):** Procesador de archivos Excel
- **MySQL:** Base de datos para persistencia

## Arquitectura del Sistema

```
Cliente (Postman/cURL) 
    ↓ POST multipart/form-data
API Gateway (Node.js:3001)
    ↓ Upload to MinIO
MinIO (localhost:9000)
    ↓ Send metadata to queue
ActiveMQ (localhost:61616)
    ↓ Consume message
DocExcelParser (WildFly:8080)
    ↓ Download from MinIO
MinIO (localhost:9000)
    ↓ Process Excel
DocExcelParser (WildFly:8080)
    ↓ Send results to queue
ActiveMQ (localhost:61616)
    ↓ Store in database
MySQL (localhost:3306)
```

## Requisitos del Sistema

### Software Requerido
- **Java 17+**
- **Node.js 18+**
- **Maven 3.6+**
- **WildFly 37.0.1.Final**
- **MySQL 8.0+**
- **ActiveMQ 6.1.7**
- **MinIO** (Docker recomendado)
- **Docker** (para MinIO)

### Puertos Utilizados
- **3001:** API Gateway
- **8080:** WildFly (DocExcelParser)
- **9000:** MinIO API
- **9001:** MinIO Console
- **61616:** ActiveMQ OpenWire
- **61613:** ActiveMQ STOMP
- **61614:** ActiveMQ WebSocket
- **3306:** MySQL

## Instalación Paso a Paso

### 1. Preparar Base de Datos MySQL

#### Instalar MySQL
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# Windows
# Descargar MySQL Installer desde https://dev.mysql.com/downloads/installer/
```

#### Configurar MySQL
```sql
-- Crear base de datos
CREATE DATABASE excelparserdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario
CREATE USER 'excelparser'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON excelparserdb.* TO 'excelparser'@'localhost';
FLUSH PRIVILEGES;

-- Verificar
SHOW DATABASES;
SELECT User, Host FROM mysql.user WHERE User = 'excelparser';
```

### 2. Instalar y Configurar MinIO

#### Instalar MinIO con Docker
```bash
# Crear directorio para datos
mkdir -p ~/minio-data

# Ejecutar MinIO
docker run -d \
  --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e "MINIO_ROOT_USER=admin" \
  -e "MINIO_ROOT_PASSWORD=admin12345" \
  -v ~/minio-data:/data \
  quay.io/minio/minio server /data --console-address ":9001"

# Verificar que esté corriendo
docker ps | grep minio
```

#### Configurar Bucket en MinIO
1. Ir a http://localhost:9001
2. Login: admin / admin12345
3. **Buckets** → **Create Bucket**
4. **Bucket Name:** `my-bucket`
5. **Region:** `us-east-1`
6. **Access Policy:** Private

### 3. Instalar y Configurar ActiveMQ

#### Descargar ActiveMQ
```bash
# Descargar ActiveMQ 6.1.7
wget https://archive.apache.org/dist/activemq/6.1.7/apache-activemq-6.1.7-bin.tar.gz
tar -xzf apache-activemq-6.1.7-bin.tar.gz
cd apache-activemq-6.1.7
```

#### Configurar ActiveMQ para Mensajes Grandes
Editar `conf/activemq.xml`:

```xml
<!-- En <transportConnectors> -->
<transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=524288000&amp;wireFormat.maxMessageSize=524288000&amp;maxCommandLength=524288000"/>
<transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=524288000&amp;wireFormat.maxMessageSize=524288000&amp;maxCommandLength=524288000"/>
```

#### Iniciar ActiveMQ
```bash
# Iniciar ActiveMQ
./bin/activemq start

# Verificar que esté corriendo
netstat -tlnp | grep 61616
```

### 4. Instalar y Configurar WildFly

#### Descargar WildFly
```bash
# Descargar WildFly 37.0.1.Final
wget https://github.com/wildfly/wildfly/releases/download/37.0.1.Final/wildfly-37.0.1.Final.tar.gz
tar -xzf wildfly-37.0.1.Final.tar.gz
cd wildfly-37.0.1.Final
```

#### Instalar MySQL JDBC Driver
```bash
# Crear estructura de módulo
mkdir -p modules/com/mysql/main

# Crear module.xml
cat > modules/com/mysql/main/module.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.8" name="com.mysql">
    <resources>
        <resource-root path="mysql-connector-j-8.0.33.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
EOF

# Descargar y copiar MySQL driver
wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar
cp mysql-connector-j-8.0.33.jar modules/com/mysql/main/
```

#### Configurar Datasource en WildFly
Editar `standalone/configuration/standalone.xml`:

```xml
<!-- En <subsystem xmlns="urn:jboss:domain:datasources:7.2"> -->
<datasource jndi-name="java:/jdbc/ExcelParserMySQLDS" 
           pool-name="ExcelParserMySQLDS" 
           enabled="true" 
           use-java-context="true">
    <connection-url>jdbc:mysql://localhost:3306/excelparserdb?useSSL=false&amp;serverTimezone=UTC&amp;allowPublicKeyRetrieval=true</connection-url>
    <driver>mysql</driver>
    <pool>
        <min-pool-size>5</min-pool-size>
        <max-pool-size>20</max-pool-size>
        <prefill>true</prefill>
        <use-strict-min>false</use-strict-min>
    </pool>
    <timeout>
        <idle-timeout-minutes>15</idle-timeout-minutes>
        <query-timeout>300</query-timeout>
        <use-try-lock>60</use-try-lock>
    </timeout>
    <security user-name="excelparser" password="password123"/>
    <validation>
        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker"/>
        <background-validation>true</background-validation>
        <background-validation-millis>30000</background-validation-millis>
        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter"/>
    </validation>
</datasource>

<!-- En <drivers> -->
<driver name="mysql" module="com.mysql">
    <driver-class>com.mysql.cj.jdbc.Driver</driver-class>
</driver>
```

#### Configurar Variables de Entorno para MinIO
Agregar en `standalone/configuration/standalone.xml`:

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

#### Iniciar WildFly
```bash
# Iniciar WildFly
./bin/standalone.sh

# Verificar que esté corriendo
netstat -tlnp | grep 8080
```

### 5. Compilar y Deployar DocExcelParser

#### Compilar Proyecto
```bash
cd DocExcelParser
mvn clean package
```

#### Deployar en WildFly
```bash
# Copiar WAR a WildFly
cp target/DocExcelParser.war $WILDFLY_HOME/standalone/deployments/

# Verificar deployment
curl http://localhost:8080/DocExcelParser/hello
```

### 6. Configurar y Ejecutar API Gateway

#### Instalar Dependencias
```bash
cd api-gateway
npm install
```

#### Configurar Variables de Entorno
Crear archivo `.env`:

```env
# MinIO Configuration
S3_ENDPOINT=http://localhost:9000
S3_REGION=us-east-1
S3_ACCESS_KEY=admin
S3_SECRET_KEY=admin12345
S3_BUCKET=my-bucket
S3_FORCE_PATH_STYLE=true

# Broker Configuration
BROKER_URL=ws://localhost:61614
BROKER_USER=admin
BROKER_PASS=admin

# Server Configuration
NODE_ENV=development
PORT=3001
```

#### Ejecutar API Gateway
```bash
node index.js
```

## Verificación del Sistema

### 1. Verificar Servicios
```bash
# Verificar MySQL
mysql -u excelparser -p -e "SHOW DATABASES;"

# Verificar MinIO
curl http://localhost:9000/minio/health/live

# Verificar ActiveMQ
curl http://localhost:8161/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost

# Verificar WildFly
curl http://localhost:8080/DocExcelParser/hello

# Verificar API Gateway
curl http://localhost:3001/api/health
```

### 2. Probar Flujo Completo
```bash
# Probar MinIO health
curl http://localhost:3001/api/minio/health

# Probar broker connection
curl -X POST http://localhost:3001/api/test-broker-connection

# Probar upload y procesamiento
curl -X POST http://localhost:3001/api/excel/upload-and-process \
  -F "file=@ejemplo_paciente.xlsx"
```

### 3. Verificar Resultados
```bash
# Ver pacientes procesados
curl http://localhost:8080/DocExcelParser/hello

# Verificar en base de datos
mysql -u excelparser -p -e "USE excelparserdb; SELECT * FROM patient_profiles;"
```

## Scripts de Automatización

### Script de Inicio Completo
```bash
#!/bin/bash
# start-system.sh

echo "🚀 Iniciando Sistema Completo de Procesamiento de Excel..."

# Iniciar MinIO
echo "📦 Iniciando MinIO..."
docker start minio || docker run -d --name minio -p 9000:9000 -p 9001:9001 -e "MINIO_ROOT_USER=admin" -e "MINIO_ROOT_PASSWORD=admin12345" -v ~/minio-data:/data quay.io/minio/minio server /data --console-address ":9001"

# Iniciar ActiveMQ
echo "📨 Iniciando ActiveMQ..."
cd apache-activemq-6.1.7
./bin/activemq start
cd ..

# Iniciar WildFly
echo "☕ Iniciando WildFly..."
cd wildfly-37.0.1.Final
./bin/standalone.sh &
cd ..

# Esperar que los servicios estén listos
echo "⏳ Esperando que los servicios estén listos..."
sleep 30

# Iniciar API Gateway
echo "🌐 Iniciando API Gateway..."
cd api-gateway
node index.js &
cd ..

echo "✅ Sistema iniciado completamente!"
echo "📊 MinIO Console: http://localhost:9001"
echo "📨 ActiveMQ Console: http://localhost:8161"
echo "☕ WildFly Console: http://localhost:9990"
echo "🌐 API Gateway: http://localhost:3001"
echo "📋 DocExcelParser: http://localhost:8080/DocExcelParser"
```

### Script de Verificación
```bash
#!/bin/bash
# verify-system.sh

echo "🔍 Verificando Sistema..."

# Verificar servicios
services=("mysql:3306" "minio:9000" "activemq:61616" "wildfly:8080" "api-gateway:3001")

for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)
    
    if netstat -tlnp | grep ":$port " > /dev/null; then
        echo "✅ $name está corriendo en puerto $port"
    else
        echo "❌ $name NO está corriendo en puerto $port"
    fi
done

# Verificar endpoints
echo "🌐 Verificando endpoints..."

curl -s http://localhost:3001/api/health > /dev/null && echo "✅ API Gateway health OK" || echo "❌ API Gateway health FAIL"
curl -s http://localhost:3001/api/minio/health > /dev/null && echo "✅ MinIO health OK" || echo "❌ MinIO health FAIL"
curl -s http://localhost:8080/DocExcelParser/hello > /dev/null && echo "✅ DocExcelParser OK" || echo "❌ DocExcelParser FAIL"
```

## Troubleshooting

### Problemas Comunes

#### 1. Puerto en Uso
```bash
# Ver qué proceso usa el puerto
netstat -tlnp | grep :3001

# Matar proceso
kill -9 $(lsof -t -i:3001)
```

#### 2. MinIO No Responde
```bash
# Verificar Docker
docker ps | grep minio

# Reiniciar MinIO
docker restart minio
```

#### 3. ActiveMQ No Conecta
```bash
# Verificar logs
tail -f apache-activemq-6.1.7/data/activemq.log

# Reiniciar ActiveMQ
cd apache-activemq-6.1.7
./bin/activemq restart
```

#### 4. WildFly No Deploya
```bash
# Verificar logs
tail -f wildfly-37.0.1.Final/standalone/log/server.log

# Verificar datasource
curl http://localhost:9990/console
```

#### 5. API Gateway No Conecta al Broker
```bash
# Verificar variables de entorno
cat api-gateway/.env

# Verificar logs
tail -f api-gateway/logs/app.log
```

## Monitoreo y Logs

### Logs Importantes
```bash
# WildFly logs
tail -f wildfly-37.0.1.Final/standalone/log/server.log

# ActiveMQ logs
tail -f apache-activemq-6.1.7/data/activemq.log

# MinIO logs
docker logs -f minio

# API Gateway logs (en consola)
```

### Métricas del Sistema
- **MinIO:** http://localhost:9001 → Monitoring
- **ActiveMQ:** http://localhost:8161 → Queues
- **WildFly:** http://localhost:9990 → Runtime
- **MySQL:** `SHOW PROCESSLIST;`

## Estado del Proyecto

✅ **MySQL:** Configurado y funcionando  
✅ **MinIO:** Instalado y configurado  
✅ **ActiveMQ:** Configurado para mensajes grandes  
✅ **WildFly:** Configurado con MySQL driver y datasource  
✅ **DocExcelParser:** Compilado y deployado  
✅ **API Gateway:** Configurado y funcionando  
✅ **Integración:** Sistema completo funcionando  
✅ **Documentación:** Completa  
✅ **Scripts:** Automatización implementada  
✅ **Troubleshooting:** Guía completa
