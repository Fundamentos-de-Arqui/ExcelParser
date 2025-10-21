# Script para agregar configuración de ActiveMQ en el lugar correcto
Write-Host "Agregando configuración de ActiveMQ en el lugar correcto..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración de ActiveMQ que necesitamos agregar
$activemqConfig = @"
                <connector name="activemq-connector" socket-binding="activemq-socket-binding"/>
"@

# Buscar la línea que contiene "</in-vm-connector>" y agregar después
$pattern = '(</in-vm-connector>)'
$replacement = "`$1`n$activemqConfig"

$newContent = $content -replace $pattern, $replacement

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "✅ Conector de ActiveMQ agregado en el lugar correcto!"
Write-Host ""
Write-Host "Ahora agregando el connection factory externo..."

# Configuración del connection factory externo
$connectionFactoryConfig = @"
                <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
"@

# Buscar la línea que contiene "RemoteConnectionFactory" y agregar después
$pattern2 = '(<connection-factory name="RemoteConnectionFactory" entries="java:jboss/exported/jms/RemoteConnectionFactory" connectors="http-connector"/>)'
$replacement2 = "`$1`n$connectionFactoryConfig"

$newContent = $newContent -replace $pattern2, $replacement2

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "✅ Connection factory externo agregado!"
Write-Host ""
Write-Host "Ahora agregando la cola externa..."

# Configuración de la cola externa
$queueConfig = @"
                <external-jms-queue name="excel-input-queue" entries="java:/jms/queue/excel-input-queue" external-context="activemq-external"/>
"@

# Buscar la línea que contiene "DLQ" y agregar después
$pattern3 = '(<jms-queue name="DLQ" entries="java:/jms/queue/DLQ"/>)'
$replacement3 = "`$1`n$queueConfig"

$newContent = $newContent -replace $pattern3, $replacement3

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "✅ Cola externa agregada!"
Write-Host ""
Write-Host "=========================================="
Write-Host "CONFIGURACIÓN COMPLETA DE ACTIVEMQ"
Write-Host "=========================================="
Write-Host "✅ Socket binding: activemq-socket-binding (puerto 61616)"
Write-Host "✅ Connector: activemq-connector"
Write-Host "✅ Connection Factory: activemq-external"
Write-Host "✅ Queue: excel-input-queue"
Write-Host ""
Write-Host "Ahora puedes reiniciar WildFly para aplicar los cambios."
