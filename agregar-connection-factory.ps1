# Script para agregar el connection factory externo de ActiveMQ
Write-Host "Agregando connection factory externo de ActiveMQ..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración del connection factory externo
$connectionFactoryConfig = @"
                <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
"@

# Buscar la línea que contiene "RemoteConnectionFactory" y agregar después
$pattern = '(<connection-factory name="RemoteConnectionFactory" entries="java:jboss/exported/jms/RemoteConnectionFactory" connectors="http-connector"/>)'
$replacement = "`$1`n$connectionFactoryConfig"

$newContent = $content -replace $pattern, $replacement

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "✅ Connection factory externo de ActiveMQ agregado!"
Write-Host ""
Write-Host "Configuración completa de ActiveMQ:"
Write-Host "- Socket binding: activemq-socket-binding (puerto 61616)"
Write-Host "- Connector: activemq-connector"
Write-Host "- Connection Factory: activemq-external"
Write-Host "- Queue: excel-input-queue"
Write-Host ""
Write-Host "Ahora puedes reiniciar WildFly para aplicar los cambios."
