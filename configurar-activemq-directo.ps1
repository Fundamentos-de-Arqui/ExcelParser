# Script para configurar ActiveMQ directamente en el archivo standalone.xml
# Este script modifica el archivo de configuración sin necesidad de que WildFly esté corriendo

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
Write-Host "PASO 2: Leyendo archivo de configuración..."
$configContent = Get-Content $wildflyConfig -Raw

Write-Host ""
Write-Host "PASO 3: Agregando configuración de ActiveMQ..."

# Configuración del socket binding para ActiveMQ
$socketBindingConfig = @"

        <socket-binding name="activemq-socket-binding" port="61616"/>
"@

# Configuración del conector para ActiveMQ
$connectorConfig = @"

        <connector name="activemq-connector" socket-binding="activemq-socket-binding"/>
"@

# Configuración del broker externo de ActiveMQ
$connectionFactoryConfig = @"

        <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
"@

# Configuración de la cola externa
$queueConfig = @"

        <external-jms-queue name="excel-input-queue" entries="java:/jms/queue/excel-input-queue" external-context="activemq-external"/>
"@

# Insertar socket binding después del último socket binding existente
$socketBindingPattern = '(<socket-binding name="[^"]*" port="[^"]*"/>)'
$configContent = $configContent -replace $socketBindingPattern, "`$1$socketBindingConfig"

# Insertar conector después del último conector existente
$connectorPattern = '(<connector name="[^"]*" socket-binding="[^"]*"/>)'
$configContent = $configContent -replace $connectorPattern, "`$1$connectorConfig"

# Insertar connection factory después del último connection factory existente
$connectionFactoryPattern = '(<connection-factory name="[^"]*" entries="[^"]*"/>)'
$configContent = $configContent -replace $connectionFactoryPattern, "`$1$connectionFactoryConfig"

# Insertar cola después del último queue existente
$queuePattern = '(<jms-queue name="[^"]*" entries="[^"]*"/>)'
$configContent = $configContent -replace $queuePattern, "`$1$queueConfig"

Write-Host ""
Write-Host "PASO 4: Guardando configuración modificada..."
$configContent | Out-File $wildflyConfig -Encoding UTF8 -NoNewline

Write-Host ""
Write-Host "=========================================="
Write-Host "CONFIGURACIÓN COMPLETADA"
Write-Host "=========================================="
Write-Host ""
Write-Host "✅ ActiveMQ configurado en WildFly"
Write-Host "✅ Backup creado en: $backupConfig"
Write-Host ""
Write-Host "Ahora puedes:"
Write-Host "1. Reiniciar WildFly"
Write-Host "2. Desplegar tu aplicación"
Write-Host "3. El JMS debería funcionar con ActiveMQ"
Write-Host ""
Write-Host "Para verificar la configuración después de reiniciar WildFly:"
Write-Host "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat"
Write-Host "/subsystem=messaging-activemq:read-resource"
Write-Host ""
Read-Host "Presiona Enter para salir"
