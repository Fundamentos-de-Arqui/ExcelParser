# Script simple para configurar ActiveMQ en WildFly
# Modifica directamente el archivo standalone.xml

Write-Host "=========================================="
Write-Host "CONFIGURACIÓN DE ACTIVEMQ EN WILDFLY"
Write-Host "=========================================="
Write-Host ""

$wildflyConfig = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"
$backupConfig = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml.backup-jms"

Write-Host "PASO 1: Creando backup del archivo de configuración..."
if (Test-Path $wildflyConfig) {
    Copy-Item $wildflyConfig $backupConfig -Force
    Write-Host "✅ Backup creado: $backupConfig"
} else {
    Write-Host "❌ No se encontró el archivo de configuración: $wildflyConfig"
    exit 1
}

Write-Host ""
Write-Host "PASO 2: Agregando configuración de ActiveMQ..."

# Leer el archivo de configuración
$configContent = Get-Content $wildflyConfig -Raw

# Configuración que necesitamos agregar
$activemqConfig = @"

        <!-- Configuración de ActiveMQ -->
        <socket-binding name="activemq-socket-binding" port="61616"/>
        <connector name="activemq-connector" socket-binding="activemq-socket-binding"/>
        <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
        <external-jms-queue name="excel-input-queue" entries="java:/jms/queue/excel-input-queue" external-context="activemq-external"/>
"@

# Buscar la sección de socket bindings y agregar nuestra configuración
$socketBindingSection = '<socket-binding-group name="standard-sockets" default-interface="public">'
$replacement = $socketBindingSection + $activemqConfig

$configContent = $configContent -replace [regex]::Escape($socketBindingSection), $replacement

Write-Host ""
Write-Host "PASO 3: Guardando configuración modificada..."
$configContent | Out-File $wildflyConfig -Encoding UTF8 -NoNewline

Write-Host ""
Write-Host "=========================================="
Write-Host "CONFIGURACIÓN COMPLETADA"
Write-Host "=========================================="
Write-Host ""
Write-Host "✅ ActiveMQ configurado en WildFly"
Write-Host "✅ Backup creado en: $backupConfig"
Write-Host ""
Write-Host "Configuración agregada:"
Write-Host "- Socket binding: activemq-socket-binding (puerto 61616)"
Write-Host "- Connector: activemq-connector"
Write-Host "- Connection Factory: activemq-external"
Write-Host "- Queue: excel-input-queue"
Write-Host ""
Write-Host "Ahora puedes:"
Write-Host "1. Reiniciar WildFly"
Write-Host "2. Desplegar tu aplicación"
Write-Host "3. El JMS debería funcionar con ActiveMQ"
Write-Host ""
Read-Host "Presiona Enter para salir"
