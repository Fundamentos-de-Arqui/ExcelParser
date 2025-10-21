# Script para agregar configuración de ActiveMQ en standalone.xml
Write-Host "Agregando configuración de ActiveMQ..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración de ActiveMQ que necesitamos agregar
$activemqConfig = @"
        <socket-binding name="activemq-socket-binding" port="61616"/>
        <connector name="activemq-connector" socket-binding="activemq-socket-binding"/>
        <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
        <external-jms-queue name="excel-input-queue" entries="java:/jms/queue/excel-input-queue" external-context="activemq-external"/>
"@

# Buscar la línea que contiene "txn-status-manager" y agregar después
$pattern = '(<socket-binding name="txn-status-manager" port="4713"/>)'
$replacement = "`$1`n$activemqConfig"

$newContent = $content -replace $pattern, $replacement

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "✅ Configuración de ActiveMQ agregada exitosamente!"
Write-Host "✅ Socket binding: activemq-socket-binding (puerto 61616)"
Write-Host "✅ Connector: activemq-connector"
Write-Host "✅ Connection Factory: activemq-external"
Write-Host "✅ Queue: excel-input-queue"
Write-Host ""
Write-Host "Ahora puedes reiniciar WildFly para aplicar los cambios."
