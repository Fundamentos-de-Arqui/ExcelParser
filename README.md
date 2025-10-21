# DocExcelParser - Parser de Excel para Formularios Médicos

## 📋 Descripción

DocExcelParser es una aplicación web Java que permite procesar archivos Excel en formato base64 y extraer datos de formularios médicos de pacientes. La aplicación incluye un sistema completo de testing que permite validar el funcionamiento del parser con cualquier archivo Excel.

## 🚀 Características

- **Parser de Excel**: Extrae datos de formularios médicos desde archivos Excel (.xlsx)
- **Validación de Base64**: Valida y limpia contenido base64 antes del procesamiento
- **Sistema de Testing**: Interfaz web para testear el parser con base64 personalizado
- **Análisis Detallado**: Muestra completitud de campos y recomendaciones
- **Integración JMS**: Conecta con ActiveMQ para procesar mensajes de cola
- **Interfaz Web**: Servlets con interfaz HTML moderna y responsive

## 🏗️ Arquitectura

```
src/main/java/com/soulware/platform/docexcelparser/
├── config/
│   └── JMSConfig.java              # Configuración JMS/ActiveMQ
├── entity/
│   └── PatientProfile.java         # Entidad del paciente
├── parser/
│   └── ExcelPatientParser.java     # Parser principal de Excel
├── service/
│   ├── PatientDataService.java     # Servicio de procesamiento
│   └── QueueReaderService.java     # Servicio de lectura de cola
├── test/
│   └── ExcelParserTester.java      # Sistema de testing
├── HelloServlet.java               # Servlet principal
└── ParserTestServlet.java          # Servlet de testing
```

## 🛠️ Tecnologías Utilizadas

- **Java 17+**
- **Jakarta EE 10**
- **Apache POI** (para procesamiento de Excel)
- **Jackson** (para serialización JSON)
- **ActiveMQ** (para mensajería JMS)
- **Maven** (para gestión de dependencias)
- **Tomcat 10** (servidor de aplicaciones)

## 📦 Instalación y Despliegue

### Prerrequisitos

1. **Java 17 o superior**
2. **Apache Maven**
3. **Apache Tomcat 10**
4. **ActiveMQ** (opcional, para funcionalidad completa)

### Pasos de Instalación

1. **Clonar/Descargar el proyecto**
   ```bash
   git clone <repository-url>
   cd DocExcelParser
   ```

2. **Compilar el proyecto**
   ```bash
   mvn clean compile
   ```

3. **Generar el WAR**
   ```bash
   mvn package
   ```

4. **Desplegar en Tomcat**
   ```bash
   # Copiar el WAR a la carpeta webapps de Tomcat
   copy target\DocExcelParser-1.0-SNAPSHOT.war C:\apache-tomcat-10.1.0\webapps\
   ```

5. **Usar el script de despliegue automático**
   ```bash
   deploy.bat
   ```

## 🌐 URLs de Acceso

Una vez desplegado, las siguientes URLs estarán disponibles:

- **Página Principal**: `http://localhost:8080/DocExcelParser-1.0-SNAPSHOT/`
- **Hello Servlet**: `http://localhost:8080/DocExcelParser-1.0-SNAPSHOT/hello-servlet`
- **Parser Test**: `http://localhost:8080/DocExcelParser-1.0-SNAPSHOT/parser-test`

## 🧪 Sistema de Testing

### Funcionalidades del Testing

1. **Test Rápido**: Prueba el parser con datos de ejemplo
2. **Test Personalizado**: Permite probar con cualquier base64 de Excel
3. **Validación Completa**: Valida formato, tamaño y estructura del archivo
4. **Análisis de Campos**: Muestra qué campos se extrajeron correctamente
5. **Recomendaciones**: Sugiere mejoras basadas en los resultados

### Cómo Usar el Testing

1. **Acceder a la interfaz de testing**:
   ```
   http://localhost:8080/DocExcelParser-1.0-SNAPSHOT/parser-test
   ```

2. **Test Rápido**:
   - Hacer clic en "⚡ Test Rápido (Ejemplo)"
   - Ver resultados inmediatamente

3. **Test Personalizado**:
   - Pegar el base64 del archivo Excel en el área de texto
   - Hacer clic en "🧪 Testear Parser"
   - Revisar resultados detallados

### Interpretación de Resultados

- **✅ Verde**: Campo extraído correctamente
- **❌ Rojo**: Campo no encontrado o vacío
- **📊 Porcentaje**: Completitud general del formulario
- **💡 Recomendaciones**: Sugerencias para mejorar la extracción

## 📊 Campos Analizados

### Campos Obligatorios
- Nombres Completos
- Apellido Paterno
- Apellido Materno
- Documento de Identidad
- Email
- Teléfono
- Dirección Actual

### Campos Opcionales
- Lugar de Nacimiento
- Fecha de Nacimiento
- Género
- Estado Civil
- Distrito/Provincia/Región
- Religión
- Grado de Instrucción
- Ocupación
- Institución Educativa Actual

## 🔧 Configuración

### ActiveMQ (Opcional)

Para funcionalidad completa con mensajería:

1. **Instalar ActiveMQ**
2. **Configurar en JMSConfig.java**:
   ```java
   private static final String BROKER_URL = "http://localhost:8161/api/message";
   private static final String QUEUE_NAME = "excel.input.queue";
   ```

3. **Credenciales por defecto**: `admin:admin`

### Base de Datos (Opcional)

Para persistencia de datos:

1. **Configurar en persistence.xml**
2. **Crear tabla patient_profile** según la entidad PatientProfile

## 🐛 Solución de Problemas

### Errores Comunes

1. **"Base64 inválido"**:
   - Verificar que el base64 esté completo
   - Asegurar que no tenga caracteres especiales
   - Verificar que el archivo sea realmente un Excel (.xlsx)

2. **"Archivo demasiado pequeño"**:
   - El archivo debe tener al menos 1KB
   - Verificar que el base64 esté completo

3. **"No se puede decodificar"**:
   - El base64 contiene caracteres inválidos
   - Usar solo caracteres A-Z, a-z, 0-9, +, /, =

4. **"No parece ser un Excel válido"**:
   - El archivo no es un .xlsx válido
   - Verificar que el archivo original sea Excel

### Logs y Debugging

Los logs detallados se muestran en la consola del servidor. Para debugging:

1. **Revisar logs de Tomcat**
2. **Usar el sistema de testing** para análisis detallado
3. **Verificar configuración de ActiveMQ** si se usa mensajería

## 📈 Mejoras Futuras

- [ ] Soporte para múltiples formatos de Excel
- [ ] Interfaz de configuración de campos
- [ ] Exportación de resultados a PDF
- [ ] Integración con más sistemas de mensajería
- [ ] API REST para integración externa
- [ ] Dashboard de estadísticas

## 🤝 Contribución

1. Fork el proyecto
2. Crear una rama para la nueva funcionalidad
3. Commit los cambios
4. Push a la rama
5. Crear un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo LICENSE para más detalles.

## 📞 Soporte

Para soporte técnico o preguntas:

- **Email**: soporte@soulware.com
- **Documentación**: Ver comentarios en el código
- **Issues**: Usar el sistema de issues del repositorio

---

**DocExcelParser v1.0** - Sistema de parsing de Excel para formularios médicos