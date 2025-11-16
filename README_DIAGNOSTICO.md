# Script de Diagnóstico de Lectura de Cola

## Descripción

Este script (`DiagnoseQueueReader.java`) replica **exactamente** la lógica de `DirectJMSListener` para diagnosticar problemas de lectura de mensajes desde ActiveMQ.

## ¿Qué hace el script?

1. ✅ Se conecta al mismo broker que tu proyecto (`tcp://172.193.242.89:61616`)
2. ✅ Usa la misma cola (`excel-input-queue`)
3. ✅ Usa la misma configuración de conexión
4. ✅ Prueba **DOS métodos** de lectura:
   - **MessageListener** (como tu proyecto)
   - **Polling Manual** (como respaldo)
5. ✅ Analiza el contenido del mensaje (JSON)
6. ✅ Muestra información detallada sobre qué está pasando

## Cómo ejecutarlo

### Opción 1: Script automático (Windows)

```batch
RunDiagnoseQueueReader.bat
```

Este script:
- Compila el proyecto
- Compila el script de diagnóstico
- Ejecuta el diagnóstico

### Opción 2: Script automático (Linux/Mac)

```bash
chmod +x RunDiagnoseQueueReader.sh
./RunDiagnoseQueueReader.sh
```

### Opción 3: Manual

```bash
# 1. Compilar el proyecto
mvn compile

# 2. Compilar el script
javac -cp "target/classes:target/DocExcelParser/WEB-INF/lib/*" DiagnoseQueueReader.java

# 3. Ejecutar
java -cp ".:target/classes:target/DocExcelParser/WEB-INF/lib/*" DiagnoseQueueReader
```

## Qué información muestra

El script muestra:

1. **Estado de la conexión**:
   - ✅ Si se conecta correctamente
   - ❌ Si hay errores de conexión

2. **Método MessageListener**:
   - ✅ Si el MessageListener se activa
   - ⚠️ Si el MessageListener NO se activa (problema común en Azure)

3. **Método Polling Manual**:
   - ✅ Si encuentra mensajes
   - ⚠️ Si no hay mensajes

4. **Análisis del mensaje**:
   - Tipo de mensaje (TextMessage/BytesMessage)
   - Contenido completo
   - Análisis JSON (fileKey, fileName, etc.)
   - Si el mensaje tiene la estructura esperada

## Interpretación de resultados

### ✅ Todo funciona correctamente

```
✅ Conexión establecida correctamente
✅ Consumer creado correctamente
✅ MessageListener funcionó correctamente
✅ Mensaje recibido y procesado
```

**Significa**: La conexión funciona, pero puede haber un problema en tu aplicación.

### ⚠️ MessageListener no se activa

```
✅ Conexión establecida correctamente
✅ Consumer creado correctamente
⚠️  MessageListener NO se activó
✅ Mensaje recibido por Polling Manual
```

**Significa**: 
- La conexión funciona
- El MessageListener tiene problemas (común en Azure/redes remotas)
- El polling manual funciona (por eso lo implementamos como respaldo)

### ❌ Error de conexión

```
❌ ERROR JMS
Error: Could not connect to broker URL: tcp://...
```

**Significa**:
- Problema de conectividad de red
- Firewall bloqueando el puerto
- El broker no está accesible

**Solución**: Ejecuta `CheckActiveMQConnection.bat` para verificar conectividad.

### ⚠️ No hay mensajes

```
✅ Conexión establecida correctamente
⚠️  No hay mensajes en la cola
```

**Significa**:
- La conexión funciona
- La cola está vacía o los mensajes ya fueron consumidos

**Solución**: 
- Verifica en la consola web de ActiveMQ si hay mensajes
- Envía un mensaje de prueba a la cola

## Comparación con tu proyecto

Este script usa **exactamente** la misma lógica que `DirectJMSListener`:

| Aspecto | DirectJMSListener | DiagnoseQueueReader |
|---------|-------------------|---------------------|
| ConnectionFactory | ActiveMQConnectionFactory | ✅ Igual |
| Broker URL | tcp://172.193.242.89:61616 | ✅ Igual |
| Queue Name | excel-input-queue | ✅ Igual |
| Session Mode | AUTO_ACKNOWLEDGE | ✅ Igual |
| MessageListener | ✅ Sí | ✅ Sí |
| Polling Manual | ✅ Sí (respaldo) | ✅ Sí (respaldo) |
| Procesamiento JSON | ✅ Sí | ✅ Sí |

## Próximos pasos después del diagnóstico

1. **Si el script funciona pero tu app no**:
   - Revisa los logs de tu aplicación
   - Verifica que el listener se esté inicializando
   - Verifica que no haya errores en el procesamiento

2. **Si el script tampoco funciona**:
   - Ejecuta `CheckActiveMQConnection.bat`
   - Verifica firewall y NSG en Azure
   - Verifica que ActiveMQ esté corriendo

3. **Si el MessageListener no se activa**:
   - Esto es normal en Azure/redes remotas
   - El polling manual debería funcionar
   - Verifica que el polling manual esté activo en tu app

## Troubleshooting

### Error: "Could not find or load main class DiagnoseQueueReader"

**Solución**: Asegúrate de compilar primero:
```bash
mvn compile
javac -cp "target/classes:target/DocExcelParser/WEB-INF/lib/*" DiagnoseQueueReader.java
```

### Error: "package javax.jms does not exist"

**Solución**: Verifica que las dependencias estén en el classpath. El proyecto usa `activemq-core` que incluye las APIs JMS.

### Error: "NoClassDefFoundError"

**Solución**: Asegúrate de incluir todas las librerías en el classpath:
```bash
java -cp ".:target/classes:target/DocExcelParser/WEB-INF/lib/*" DiagnoseQueueReader
```

## Contacto

Si después de ejecutar este script sigues teniendo problemas, comparte:
1. La salida completa del script
2. Los logs de tu aplicación
3. El resultado de `CheckActiveMQConnection.bat`

