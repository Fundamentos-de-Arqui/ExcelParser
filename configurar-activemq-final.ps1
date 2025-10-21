# Script para configurar ActiveMQ correctamente en WildFly
Write-Host "Configurando ActiveMQ correctamente en WildFly..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

Write-Host "PASO 1: Agregando socket binding..."

# Configuración del socket binding de ActiveMQ
$socketBindingConfig = @"
        <socket-binding name="activemq-socket-binding" port="61616"/>
"@

# Buscar la línea que contiene "txn-status-manager" y agregar después
$pattern1 = '(<socket-binding name="txn-status-manager" port="4713"/>)'
$replacement1 = "`$1`n$socketBindingConfig"

$newContent = $content -replace $pattern1, $replacement1

Write-Host "PASO 2: Agregando conector dentro del subsistema messaging-activemq..."

# Configuración del conector de ActiveMQ
$connectorConfig = @"
                <connector name="activemq-connector" socket-binding="activemq-socket-binding"/>
"@

# Buscar la línea que contiene "</in-vm-connector>" y agregar después
$pattern2 = '(</in-vm-connector>)'
$replacement2 = "`$1`n$connectorConfig"

$newContent = $newContent -replace $pattern2, $replacement2

Write-Host "PASO 3: Agregando connection factory externo dentro del subsistema..."

# Configuración del connection factory externo
$connectionFactoryConfig = @"
                <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
"@

# Buscar la línea que contiene "RemoteConnectionFactory" y agregar después
$pattern3 = '(<connection-factory name="RemoteConnectionFactory" entries="java:jboss/exported/jms/RemoteConnectionFactory" connectors="http-connector"/>)'
$replacement3 = "`$1`n$connectionFactoryConfig"

$newContent = $newContent -replace $pattern3, $replacement3

Write-Host "PASO 4: Agregando external-jms-queue al mismo nivel que el subsistema..."

# Configuración de la cola externa (debe ir al mismo nivel que el subsistema)
$queueConfig = @"
        <external-jms-queue name="excel-input-queue" entries="java:/jms/queue/excel-input-queue" external-context="activemq-external"/>
"@

# Buscar la línea que contiene "</subsystem>" del messaging-activemq y agregar después
$pattern4 = '(</subsystem>)'
$replacement4 = "`$1`n$queueConfig"

$newContent = $newContent -replace $pattern4, $replacement4

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "=========================================="
Write-Host "CONFIGURACIÓN COMPLETA DE ACTIVEMQ"
Write-Host "=========================================="
Write-Host "✅ Socket binding: activemq-socket-binding (puerto 61616)"
Write-Host "✅ Connector: activemq-connector"
Write-Host "✅ Connection Factory: activemq-external"
Write-Host "✅ External Queue: excel-input-queue"
Write-Host ""
Write-Host "Ahora puedes reiniciar WildFly para aplicar los cambios."
