# Troubleshooting: ActiveMQ en Azure - Problemas de Conexión

## Problemas Comunes con ActiveMQ en Azure

Cuando ActiveMQ está hosteado en Azure, pueden surgir varios problemas de conectividad:

### 1. **Firewall y Network Security Groups (NSG)**
- **Problema**: Los puertos pueden estar bloqueados por NSG de Azure
- **Solución**: Verificar que los puertos estén abiertos:
  - Puerto 61616 (JMS/TCP)
  - Puerto 8161 (Consola Web HTTP)
  - Puerto 5672 (AMQP, si se usa)

### 2. **NAT y Latencia de Red**
- **Problema**: La latencia puede causar timeouts
- **Solución**: El polling manual implementado ayuda a manejar esto

### 3. **Keep-Alive y Conexiones Persistentes**
- **Problema**: Las conexiones pueden cerrarse por inactividad
- **Solución**: El ExceptionListener detecta estos problemas

### 4. **MessageListener No Se Activa**
- **Problema**: En redes remotas, el MessageListener puede no activarse
- **Solución**: El polling manual cada 3 segundos actúa como respaldo

## Cómo Verificar la Conexión desde tu PC

### Método 1: Script de Verificación (Windows)

Ejecuta el script `CheckActiveMQConnection.bat`:

```batch
CheckActiveMQConnection.bat
```

Este script verifica:
1. ✅ Conectividad de red (ping)
2. ✅ Puerto JMS (61616)
3. ✅ Consola web HTTP (8161)
4. ✅ API REST de ActiveMQ

### Método 2: Verificación Manual

#### Paso 1: Verificar Conectividad de Red
```powershell
ping 172.193.242.89
```

#### Paso 2: Verificar Puerto JMS (61616)
```powershell
Test-NetConnection -ComputerName 172.193.242.89 -Port 61616
```

O usando telnet:
```cmd
telnet 172.193.242.89 61616
```

#### Paso 3: Verificar Consola Web (8161)
Abre en tu navegador:
```
http://172.193.242.89:8161
```

Usuario: `admin` / Contraseña: `admin`

#### Paso 4: Verificar API REST
```powershell
$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('admin:admin'))
Invoke-WebRequest -Uri "http://172.193.242.89:8161/api/jolokia/version" -Headers @{Authorization=("Basic " + $cred)}
```

### Método 3: Test Java (Recomendado)

Compila y ejecuta `TestActiveMQConnection.java`:

```bash
# Si tienes Maven
mvn compile exec:java -Dexec.mainClass="TestActiveMQConnection" -Dexec.classpathScope=test

# O manualmente
javac -cp "activemq-all-5.x.x.jar" TestActiveMQConnection.java
java -cp ".:activemq-all-5.x.x.jar" TestActiveMQConnection
```

Este test:
- ✅ Intenta conectarse al broker
- ✅ Crea una sesión
- ✅ Intenta recibir mensajes
- ✅ Muestra errores detallados si hay problemas

## Verificar en Azure

### 1. Network Security Groups (NSG)

En Azure Portal:
1. Ve a tu VM/Recurso de ActiveMQ
2. Busca "Networking" o "Network Security Group"
3. Verifica reglas de entrada (Inbound Rules):
   - Puerto 61616 (TCP) - Permitir desde tu IP o Internet
   - Puerto 8161 (TCP) - Permitir desde tu IP o Internet

### 2. Firewall de la VM

Si ActiveMQ está en una VM de Azure, verifica el firewall del sistema operativo:

**Windows:**
```powershell
# Verificar reglas de firewall
Get-NetFirewallRule | Where-Object {$_.DisplayName -like "*61616*" -or $_.DisplayName -like "*8161*"}
```

**Linux:**
```bash
# Verificar iptables
sudo iptables -L -n | grep 61616
sudo iptables -L -n | grep 8161
```

### 3. Verificar que ActiveMQ Esté Corriendo

**En el servidor Azure:**
```bash
# Verificar proceso
ps aux | grep activemq

# Verificar puertos en uso
netstat -tulpn | grep 61616
netstat -tulpn | grep 8161
```

## Soluciones Implementadas

### 1. Polling Manual como Respaldo
- Se ejecuta cada 3 segundos
- Funciona incluso si el MessageListener no se activa
- Es especialmente útil para conexiones remotas

### 2. Exception Listener
- Detecta problemas de conexión automáticamente
- Registra errores en los logs

### 3. Configuración Mejorada
- ClientID único por conexión
- Configuración optimizada para redes remotas

## Logs a Revisar

Cuando hay problemas, revisa estos logs:

1. **Logs de la aplicación**:
   - Busca "JMS CONNECTION EXCEPTION"
   - Busca "POLLING MANUAL"
   - Busca "MESSAGE RECEIVED"

2. **Logs de ActiveMQ** (en el servidor):
   - `activemq.log`
   - Busca conexiones entrantes
   - Busca errores de autenticación

## Próximos Pasos si No Funciona

1. ✅ Ejecuta `CheckActiveMQConnection.bat`
2. ✅ Verifica NSG en Azure
3. ✅ Verifica firewall de la VM
4. ✅ Verifica que ActiveMQ esté corriendo
5. ✅ Revisa logs de ActiveMQ en el servidor
6. ✅ Prueba con `TestActiveMQConnection.java`

## Contacto y Soporte

Si después de todas estas verificaciones sigue sin funcionar:
- Revisa los logs detallados
- Verifica la configuración de ActiveMQ en el servidor
- Considera usar VPN o punto de conexión privado en Azure

