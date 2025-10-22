# MQListeningTest

Proyecto de prueba para verificar si el problema de JMS es de WildFly o de ActiveMQ.

## Descripción

Este proyecto es una aplicación Java independiente que:
- Se conecta directamente a ActiveMQ (sin WildFly)
- Escucha mensajes JSON de la cola `excel-input-queue`
- Envía mensajes de prueba a la misma cola

## Requisitos

- Java 17+
- Maven 3.6+
- ActiveMQ corriendo en `localhost:61616`

## Uso

### 1. Ejecutar el Listener

```powershell
.\ejecutar-listener.ps1
```

Esto iniciará el listener que escuchará mensajes de la cola.

### 2. Enviar Mensaje de Prueba

En otra terminal:

```powershell
.\enviar-mensaje.ps1
```

Esto enviará un mensaje JSON de prueba a la cola.

### 3. Verificar Resultados

Si el listener recibe el mensaje, verás:

```
=== MESSAGE RECEIVED BY MQ LISTENER TEST ===
=== TIMESTAMP: 2025-10-21T18:20:00 ===
Message type: TextMessage
Message length: 198 characters
Message content:
{
    "messageId": "test-direct-1234567890",
    "fileName": "test-direct.xlsx",
    "status": "DIRECT_TEST",
    ...
}
```

## Diagnóstico

- **Si el listener recibe mensajes**: El problema es de WildFly
- **Si el listener NO recibe mensajes**: El problema es de ActiveMQ o configuración de red

## Estructura del Proyecto

```
MQListeningTest/
├── pom.xml
├── src/main/java/com/soulware/platform/mqtest/
│   ├── MQListenerTest.java      # Listener que escucha mensajes
│   └── MQMessageSenderTest.java # Sender que envía mensajes
├── ejecutar-listener.ps1        # Script para ejecutar listener
├── enviar-mensaje.ps1           # Script para enviar mensaje
└── README.md                    # Este archivo
```

## Configuración

El proyecto está configurado para conectarse a:
- **Broker**: `tcp://localhost:61616`
- **Cola**: `excel-input-queue`

Si necesitas cambiar la configuración, edita las constantes en las clases Java.