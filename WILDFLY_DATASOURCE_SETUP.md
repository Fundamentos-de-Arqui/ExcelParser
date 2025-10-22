# Configuración de Datasource MySQL en WildFly

## Problema Resuelto

El error original era:
```
WFLYCTL0412: Required services that are not installed: ["jboss.jdbc-driver.ExcelParser-1_0-SNAPSHOT_war_com_mysql_cj_jdbc_Driver_8_0"]
```

## Cambios Realizados

### 1. Dependencias Agregadas al pom.xml
- **MySQL JDBC Driver**: `com.mysql:mysql-connector-j:8.0.33`
- **Jakarta Persistence API**: `jakarta.persistence:jakarta.persistence-api:3.1.0`
- **Hibernate Validator**: `org.hibernate.validator:hibernate-validator:8.0.1.Final`

### 2. Configuración de persistence.xml
- Datasource JNDI: `java:/jdbc/ExcelParserMySQLDS`
- Entidades JPA mapeadas: `PatientProfile`, `LegalGuardian`, `ReferredTherapist`
- Propiedades Hibernate configuradas para MySQL8

### 3. Correcciones de Sintaxis
- Arreglado error en `PatientProfile.java` línea 85

## Configuración Requerida en WildFly

### Opción 1: Usando la Consola Web de WildFly

1. Acceder a: `http://localhost:9990/console`
2. Ir a **Configuration** → **Subsystems** → **Datasources**
3. Hacer clic en **Add** → **Add Datasource**
4. Configurar:
   - **Name**: `ExcelParserMySQLDS`
   - **JNDI Name**: `java:/jdbc/ExcelParserMySQLDS`
   - **Driver**: Seleccionar `mysql` (debe estar instalado)
   - **Connection URL**: `jdbc:mysql://localhost:3306/excel_parser_db`
   - **Username**: `tu_usuario_mysql`
   - **Password**: `tu_password_mysql`

### Opción 2: Usando CLI de WildFly

```bash
# Conectar a WildFly CLI
./jboss-cli.sh --connect

# Instalar driver MySQL (si no está instalado)
/subsystem=datasources/jdbc-driver=mysql:add(driver-name=mysql,driver-module-name=com.mysql,driver-class-name=com.mysql.cj.jdbc.Driver)

# Crear datasource
/subsystem=datasources/data-source=ExcelParserMySQLDS:add(jndi-name=java:/jdbc/ExcelParserMySQLDS,driver-name=mysql,connection-url=jdbc:mysql://localhost:3306/excel_parser_db,user-name=tu_usuario,password=tu_password)

# Habilitar datasource
/subsystem=datasources/data-source=ExcelParserMySQLDS:enable
```

### Opción 3: Modificar standalone.xml ✅ **IMPLEMENTADO**

**Ubicación**: `C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\standalone\configuration\standalone.xml`

**Configuración aplicada**:

```xml
<subsystem xmlns="urn:jboss:domain:datasources:7.2">
    <datasources>
        <!-- Datasource MySQL configurado -->
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
        
        <!-- Driver MySQL agregado -->
        <drivers>
            <driver name="mysql" module="com.mysql">
                <driver-class>com.mysql.cj.jdbc.Driver</driver-class>
            </driver>
        </drivers>
    </datasources>
</subsystem>
```

**Cambios realizados**:
- ✅ Corregido nombre del driver de `ExcelParser-1.0-SNAPSHOT.war_com.mysql.cj.jdbc.Driver_8_0` a `mysql`
- ✅ Agregado `enabled="true"` y `use-java-context="true"`
- ✅ Mejorada URL de conexión con parámetros MySQL8
- ✅ Agregado pool de conexiones optimizado
- ✅ Agregado timeouts y validación de conexión
- ✅ Agregado driver MySQL en sección drivers
- ✅ **Instalado módulo MySQL**: Creado `modules/com/mysql/main/` con driver y module.xml

## Instalación del Módulo MySQL ✅ **COMPLETADO**

**Problema identificado**: WildFly no tenía el módulo MySQL instalado, causando el error del driver.

**Solución implementada**:

### 1. Crear estructura del módulo
```bash
mkdir "C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\modules\com\mysql\main"
```

### 2. Crear archivo module.xml
**Ubicación**: `C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\modules\com\mysql\main\module.xml`

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

### 3. Copiar driver MySQL
```bash
copy "target\DocExcelParser\WEB-INF\lib\mysql-connector-j-8.0.33.jar" "C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\modules\com\mysql\main\"
```

**Resultado**: Módulo MySQL instalado correctamente en WildFly.

## Base de Datos MySQL

### Crear la Base de Datos

```sql
CREATE DATABASE excelparserdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usuario ya configurado: root con password tumadrexd789
-- Si necesitas crear un usuario específico:
CREATE USER 'excel_parser_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON excelparserdb.* TO 'excel_parser_user'@'localhost';
FLUSH PRIVILEGES;
```

**Nota**: La configuración actual usa el usuario `root` con password `tumadrexd789` y la base de datos `excelparserdb`.

### Tablas

Las tablas se crearán automáticamente gracias a la configuración `hibernate.hbm2ddl.auto=update` en `persistence.xml`.

## Verificación

### 1. **Verificar Configuración del standalone.xml**
```bash
# Verificar que el archivo se modificó correctamente
findstr /n "ExcelParserMySQLDS" "C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\standalone\configuration\standalone.xml"
```

### 2. **Iniciar WildFly**
```bash
# Detener WildFly si está ejecutándose
# Luego iniciar desde:
C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\bin\standalone.bat
```

### 3. **Verificar Datasource en CLI**
```bash
# Conectar a WildFly CLI
C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\bin\jboss-cli.bat --connect

# Verificar datasource
/subsystem=datasources/data-source=ExcelParserMySQLDS:read-resource

# Probar conexión
/subsystem=datasources/data-source=ExcelParserMySQLDS:test-connection-in-pool
```

### 4. **Verificar en Consola Web**
- Acceder a: `http://localhost:9990/console`
- Ir a **Configuration** → **Subsystems** → **Datasources**
- Verificar que `ExcelParserMySQLDS` aparece como **Enabled**

### 5. **Deployar y Probar Aplicación**
1. **Compilar proyecto**: `mvn clean package`
2. **Deployar WAR**: Copiar `target/DocExcelParser.war` a `C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\standalone\deployments\`
3. **Verificar logs**: Revisar que no aparezcan errores de datasource
4. **Probar aplicación**: Acceder a `http://localhost:8080/DocExcelParser/`

## Notas Importantes

- Asegúrate de que MySQL esté ejecutándose en el puerto 3306
- Las credenciales de la base de datos deben ser correctas
- El driver MySQL debe estar instalado en WildFly
- El datasource debe estar habilitado antes del deploy

## Estado del Proyecto

✅ **Compilación**: Exitosa  
✅ **Empaquetado**: WAR generado correctamente  
✅ **Dependencias**: Todas agregadas  
✅ **Configuración JPA**: Completa  
✅ **Configuración Datasource**: Implementada en standalone.xml  
✅ **Driver MySQL**: Configurado correctamente  
⏳ **Deploy**: Listo para probar - solo falta iniciar WildFly y deployar

## Resumen de Configuración Implementada

- **Ubicación WildFly**: `C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final`
- **Base de Datos**: `excelparserdb` en MySQL localhost:3306
- **Usuario**: `root` / Password: `tumadrexd789`
- **JNDI Name**: `java:/jdbc/ExcelParserMySQLDS`
- **Pool**: 5-20 conexiones con validación automática
- **Driver**: MySQL Connector/J configurado correctamente
