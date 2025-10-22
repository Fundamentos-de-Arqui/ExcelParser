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

### Opción 3: Modificar standalone.xml

Agregar en `standalone.xml` dentro de `<subsystem xmlns="urn:jboss:domain:datasources:7.0">`:

```xml
<datasource jndi-name="java:/jdbc/ExcelParserMySQLDS" pool-name="ExcelParserMySQLDS" enabled="true">
    <connection-url>jdbc:mysql://localhost:3306/excel_parser_db</connection-url>
    <driver>mysql</driver>
    <pool>
        <min-pool-size>5</min-pool-size>
        <max-pool-size>20</max-pool-size>
    </pool>
    <security>
        <user-name>tu_usuario</user-name>
        <password>tu_password</password>
    </security>
</datasource>
```

## Base de Datos MySQL

### Crear la Base de Datos

```sql
CREATE DATABASE excel_parser_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear usuario (opcional)
CREATE USER 'excel_parser_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON excel_parser_db.* TO 'excel_parser_user'@'localhost';
FLUSH PRIVILEGES;
```

### Tablas

Las tablas se crearán automáticamente gracias a la configuración `hibernate.hbm2ddl.auto=update` en `persistence.xml`.

## Verificación

1. **Compilar proyecto**: `mvn clean package`
2. **Deployar WAR**: Copiar `target/DocExcelParser.war` a `wildfly/standalone/deployments/`
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
⏳ **Deploy**: Pendiente de configuración de datasource en WildFly
