# Solución: Mensajes Bloqueados en ActiveMQ

## Problema Identificado

Los mensajes están **bloqueados/reservados** por el consumer de `DirectJMSListener` que está corriendo en WildFly, pero ese consumer **NO los está procesando**.

### Evidencia:
1. ✅ La conexión funciona correctamente
2. ✅ `QueueBrowser` puede VER los mensajes (hay 2 mensajes en la cola)
3. ❌ `receive()` NO puede leer los mensajes (están reservados por otro consumer)
4. ❌ El MessageListener NO se activa (problema común en Azure)

## Causa Raíz

Cuando ActiveMQ entrega mensajes a un consumer:
- Los mensajes se "reservan" para ese consumer
- Si el consumer tiene un MessageListener que no se activa, los mensajes quedan bloqueados
- Otros consumers NO pueden leer esos mensajes hasta que:
  - El consumer original los procesa
  - El consumer original se cierra
  - Los mensajes expiran o se redeliveran

## Soluciones

### Solución 1: Reiniciar WildFly (RECOMENDADO)

**Pasos:**
1. Detén WildFly completamente
2. Esto cerrará el consumer que tiene los mensajes bloqueados
3. Los mensajes volverán a estar disponibles
4. Reinicia WildFly con el código actualizado
5. El nuevo código (sin MessageListener, solo polling manual) debería funcionar

**Ventajas:**
- Libera los mensajes bloqueados inmediatamente
- Permite que el nuevo código funcione correctamente

### Solución 2: Eliminar Mensajes Bloqueados desde la Consola Web

**Pasos:**
1. Abre la consola web de ActiveMQ: `http://172.193.242.89:8161`
2. Ve a la cola `excel-input-queue`
3. Haz clic en "View Consumers"
4. Verifica qué consumers están activos
5. Si es posible, elimina los mensajes bloqueados manualmente
6. O espera a que expiren (si tienen expiración configurada)

### Solución 3: Configurar Redelivery Policy

Agregar configuración para que los mensajes se redeliveran si no se procesan:

```java
// En DirectJMSListener, después de crear la conexión:
RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
redeliveryPolicy.setMaximumRedeliveries(3);
redeliveryPolicy.setInitialRedeliveryDelay(1000);
redeliveryPolicy.setRedeliveryDelay(2000);
factory.setRedeliveryPolicy(redeliveryPolicy);
```

### Solución 4: Usar CLIENT_ACKNOWLEDGE en lugar de AUTO_ACKNOWLEDGE

Esto permite más control sobre cuándo se confirma el mensaje:

```java
session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
// Luego en processMessage():
message.acknowledge(); // Solo después de procesar exitosamente
```

## Cambios Implementados en el Código

1. ✅ **Eliminado MessageListener**: Ya no se configura (no funciona bien en Azure)
2. ✅ **Polling Manual Mejorado**: Usa el consumer principal directamente
3. ✅ **Prefetch=1**: Configurado para evitar que se reserven múltiples mensajes
4. ✅ **Recreación de Consumer**: Si hay errores, se recrea automáticamente

## Verificación Post-Solución

Después de reiniciar WildFly, verifica:

1. **Logs de inicio**:
   ```
   === INICIANDO POLLING MANUAL ===
   ✅ Consumer creado para polling manual
   ```

2. **Logs de polling** (cada 60 segundos):
   ```
   Polling activo (Poll #20) - No hay mensajes en la cola
   ```

3. **Cuando llegue un mensaje**:
   ```
   === MENSAJE RECIBIDO POR POLLING MANUAL ===
   === POLL #X ===
   ```

## Si Sigue Sin Funcionar

1. **Verifica que WildFly esté completamente detenido**:
   ```bash
   # Verificar procesos
   jps -l | grep wildfly
   ```

2. **Verifica que no haya otros consumers activos**:
   - Consola web de ActiveMQ → Queues → excel-input-queue → View Consumers

3. **Ejecuta el script de diagnóstico**:
   ```bash
   java -cp ".;target/classes;target/DocExcelParser/WEB-INF/lib/*" DiagnoseQueueReader
   ```

4. **Verifica los logs de WildFly**:
   - Busca errores de JMS
   - Busca "POLLING MANUAL"
   - Busca "MENSAJE RECIBIDO"

## Próximos Pasos Inmediatos

1. **DETÉN WildFly completamente**
2. **Espera 10 segundos** (para que se cierren todas las conexiones)
3. **Verifica en la consola web** que no haya consumers activos
4. **Reinicia WildFly** con el código actualizado
5. **Monitorea los logs** para ver si el polling manual funciona

