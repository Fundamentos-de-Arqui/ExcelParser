# Script para agregar connection factory externo manualmente
Write-Host "Agregando connection factory externo manualmente..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración del connection factory externo
$connectionFactoryConfig = @"
                <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
"@

# Buscar específicamente el pooled-connection-factory y agregar después
$pattern = '(<pooled-connection-factory name="activemq-ra" entries="java:/JmsXA java:jboss/DefaultJMSConnectionFactory" connectors="in-vm" transaction="xa"/>)'
$replacement = "`$1`n$connectionFactoryConfig"

$newContent = $content -replace $pattern, $replacement

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "Connection factory externo agregado manualmente!"
Write-Host "Configuracion completa de ActiveMQ lista!"
