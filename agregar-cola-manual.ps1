# Script para agregar cola manualmente
Write-Host "Agregando cola excel-input-queue manualmente..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración de la cola
$queueConfig = @"
                <jms-queue name="excel-input-queue" entries="java:/jms/queue/excel-input-queue"/>
"@

# Buscar específicamente el pooled-connection-factory y agregar después
$pattern = '(<pooled-connection-factory name="activemq-ra" entries="java:/JmsXA java:jboss/DefaultJMSConnectionFactory" connectors="in-vm" transaction="xa"/>)'
$replacement = "`$1`n$queueConfig"

$newContent = $content -replace $pattern, $replacement

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "Cola excel-input-queue agregada manualmente!"
Write-Host "Configuracion completa lista!"
