# DocExcelParser - Sistema de Procesamiento de Archivos Excel

## Descripción
Sistema Java que procesa archivos Excel de formularios médicos y extrae información de pacientes, responsables legales y médicos tratantes. Se despliega en WildFly y se integra con ActiveMQ para el procesamiento asíncrono.

## Arquitectura del Sistema

```
API Gateway (Node.js) → ActiveMQ → DocExcelParser (WildFly) → ActiveMQ → Resultados
```

## Características

- ✅ **Procesamiento de Excel:** Extrae datos de formularios médicos
- ✅ **Entidades JPA:** PatientProfile, LegalGuardian, ReferredTherapist
- ✅ **Integración ActiveMQ:** Procesamiento asíncrono via JMS
- ✅ **Integración MinIO:** Descarga de archivos desde object storage
- ✅ **Base de Datos MySQL:** Persistencia de datos procesados
- ✅ **Logging Detallado:** Para debugging y monitoreo

## Requisitos del Sistema

### Software Requerido
- **Java 17+**
- **Maven 3.6+**
- **WildFly 37.0.1.Final**
- **MySQL 8.0+**
- **ActiveMQ 6.1.7**
- **MinIO** (para object storage)

### Dependencias Maven
```xml
<dependencies>
    <!-- MySQL JDBC Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- AWS SDK for S3/MinIO -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>s3</artifactId>
        <version>2.20.162</version>
    </dependency>
    
    <!-- AWS SDK Core -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>aws-core</artifactId>
        <version>2.20.162</version>
    </dependency>
    
    <!-- AWS SDK Auth -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>auth</artifactId>
        <version>2.20.162</version>
    </dependency>
    
    <!-- Jakarta Persistence API -->
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- Hibernate Validator -->
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.1.Final</version>
    </dependency>
</dependencies>
```

## Configuración de WildFly

### 1. Instalar MySQL JDBC Driver

#### Crear estructura de módulo:
```bash
mkdir -p $WILDFLY_HOME/modules/com/mysql/main
```

#### Crear archivo module.xml:
```xml
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
```

#### Copiar JAR del driver:
```bash
cp mysql-connector-j-8.0.33.jar $WILDFLY_HOME/modules/com/mysql/main/
```

### 2. Configurar Datasource en standalone.xml

Agregar en `<subsystem xmlns="urn:jboss:domain:datasources:7.2">`:

```xml
<!-- Datasource MySQL -->
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
    <security user-name="root" password="tumadrexd789"/>
    <validation>
        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker"/>
        <background-validation>true</background-validation>
        <background-validation-millis>30000</background-validation-millis>
        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter"/>
    </validation>
</datasource>

<!-- Drivers -->
<drivers>
    <driver name="h2" module="com.h2database.h2">
        <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
    </driver>
    <driver name="mysql" module="com.mysql">
        <driver-class>com.mysql.cj.jdbc.Driver</driver-class>
    </driver>
</drivers>
```

### 3. Configurar Base de Datos MySQL

#### Crear base de datos:
```sql
CREATE DATABASE excelparserdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### Usuario y permisos:
```sql
CREATE USER 'excelparser'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON excelparserdb.* TO 'excelparser'@'localhost';
FLUSH PRIVILEGES;
```

## Configuración de ActiveMQ

### Modificar activemq.xml

Agregar configuración para mensajes grandes en `<transportConnectors>`:

```xml
<transportConnector name="stomp" uri="stomp://0.0.0.0:61613?maximumConnections=1000&amp;wireFormat.maxFrameSize=524288000&amp;wireFormat.maxMessageSize=524288000&amp;maxCommandLength=524288000"/>
<transportConnector name="ws" uri="ws://0.0.0.0:61614?maximumConnections=1000&amp;wireFormat.maxFrameSize=524288000&amp;wireFormat.maxMessageSize=524288000&amp;maxCommandLength=524288000"/>
```

## Configuración de MinIO

### Variables de Entorno
El DocExcelParser necesita estas variables de entorno:

```bash
# MinIO Configuration
S3_ENDPOINT=http://localhost:9000
S3_REGION=us-east-1
S3_ACCESS_KEY=admin
S3_SECRET_KEY=admin12345
S3_BUCKET=my-bucket
S3_FORCE_PATH_STYLE=true
```

### Configurar en WildFly
Agregar en `standalone.xml` en `<system-properties>`:

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

## Compilación y Deployment

### 1. Compilar el proyecto:
```bash
mvn clean package
```

### 2. Copiar WAR a WildFly:
```bash
cp target/DocExcelParser.war $WILDFLY_HOME/standalone/deployments/
```

### 3. Iniciar WildFly:
```bash
$WILDFLY_HOME/bin/standalone.sh
```

### 4. Verificar deployment:
- **Admin Console:** http://localhost:9990
- **Aplicación:** http://localhost:8080/DocExcelParser

## Estructura del Proyecto

```
src/main/java/com/soulware/platform/docexcelparser/
├── entity/
│   ├── PatientProfile.java          # Entidad principal del paciente
│   ├── LegalGuardian.java           # Responsables legales
│   └── ReferredTherapist.java       # Médico tratante
├── listener/
│   └── DirectJMSListener.java      # Listener JMS para procesar mensajes
├── parser/
│   └── ExcelPatientParser.java     # Parser de archivos Excel
├── service/
│   ├── MinioService.java           # Servicio para MinIO
│   └── PatientJSONSenderService.java # Envío de resultados
└── servlet/
    └── HelloServlet.java           # Servlet para monitoreo
```

## Entidades JPA

### PatientProfile
```java
@Entity
@Table(name = "patient_profiles")
public class PatientProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "first_names")
    private String firstNames;
    
    @Column(name = "paternal_surname")
    private String paternalSurname;
    
    @Column(name = "maternal_surname")
    private String maternalSurname;
    
    @Column(name = "identity_document_number")
    private String identityDocumentNumber;
    
    // ... más campos
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<LegalGuardian> legalGuardians;
    
    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL)
    private ReferredTherapist referredTherapist;
}
```

### LegalGuardian
```java
@Entity
@Table(name = "legal_guardians")
public class LegalGuardian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "full_name")
    private String fullName;
    
    @Column(name = "identity_document_number")
    private String identityDocumentNumber;
    
    @Column(name = "relationship")
    private String relationship;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "email")
    private String email;
    
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private PatientProfile patient;
}
```

## Flujo de Procesamiento

### 1. Recepción de Mensaje
```java
@WebListener
public class DirectJMSListener implements ServletContextListener {
    // Escucha en cola: excel-input-queue
    // Procesa mensajes con fileKey de MinIO
}
```

### 2. Descarga desde MinIO
```java
public class MinioService {
    public byte[] downloadFile(String key) throws IOException {
        // Descarga archivo desde MinIO usando fileKey
    }
}
```

### 3. Parsing de Excel
```java
public class ExcelPatientParser {
    public PatientProfile parsePatientFromExcel(String base64Content) {
        // Extrae datos del formulario médico
        // Procesa responsables legales (R.1, R.2)
        // Extrae médico tratante
    }
}
```

### 4. Envío de Resultados
```java
public class PatientJSONSenderService {
    public boolean sendPatientDataToQueue(PatientProfile patient) {
        // Envía JSON procesado a cola: patient-data-queue
    }
}
```

## Monitoreo y Debugging

### Servlet de Monitoreo
- **URL:** http://localhost:8080/DocExcelParser/hello
- **Funciones:**
  - Ver estado del listener JMS
  - Ver últimos mensajes procesados
  - Ver pacientes procesados
  - Verificar conexión con ActiveMQ

### Logs Importantes
```bash
# Logs de WildFly
tail -f $WILDFLY_HOME/standalone/log/server.log

# Buscar logs específicos
grep "DirectJMSListener" $WILDFLY_HOME/standalone/log/server.log
grep "ExcelPatientParser" $WILDFLY_HOME/standalone/log/server.log
```

## Troubleshooting

### Error: "Cannot find module com.mysql"
**Solución:** Verificar que el módulo MySQL esté instalado correctamente en WildFly

### Error: "Datasource not found"
**Solución:** Verificar configuración del datasource en standalone.xml

### Error: "MinIO connection failed"
**Solución:** Verificar variables de entorno y que MinIO esté corriendo

### Error: "ActiveMQ connection refused"
**Solución:** Verificar que ActiveMQ esté corriendo en puerto 61616

## Testing

### Probar con API Gateway
1. **Subir archivo:** POST `/api/excel/upload-and-process`
2. **Verificar logs:** WildFly debe mostrar procesamiento
3. **Verificar resultados:** GET `/api/hello` para ver pacientes procesados

### Probar directamente
```bash
# Enviar mensaje de prueba a ActiveMQ
curl -X POST http://localhost:61614/api/message/excel-input-queue \
  -H "Content-Type: application/json" \
  -d '{"fileKey":"test-key","fileName":"test.xlsx"}'
```

## Estado del Proyecto

✅ **Entidades JPA:** Implementadas  
✅ **Parser Excel:** Implementado  
✅ **Integración ActiveMQ:** Implementada  
✅ **Integración MinIO:** Implementada  
✅ **Deployment WildFly:** Configurado  
✅ **Base de Datos MySQL:** Configurada  
✅ **Logging:** Implementado  
✅ **Monitoreo:** Implementado  
✅ **Documentación:** Completa