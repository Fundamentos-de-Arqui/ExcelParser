# Script para agregar conector y connection factory de ActiveMQ
Write-Host "Agregando conector y connection factory de ActiveMQ..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración del conector de ActiveMQ
$connectorConfig = @"
                <connector name="activemq-connector" socket-binding="activemq-socket-binding"/>
"@

# Configuración del connection factory externo
$connectionFactoryConfig = @"
                <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
"@

# Configuración de la cola externa
$queueConfig = @"
                <external-jms-queue name="excel-input-queue" entries="java:/jms/queue/excel-input-queue" external-context="activemq-external"/>
"@

# Buscar la línea que contiene "in-vm-connector" y agregar después
$pattern = '(<in-vm-connector name="in-vm" server-id="0">)'
$replacement = "`$1`n$connectorConfig"

$newContent = $content -replace $pattern, $replacement

# Buscar la línea que contiene "connection-factory" y agregar después
$pattern2 = '(<connection-factory name="InVmConnectionFactory" connector="in-vm" entries="java:/ConnectionFactory"/>)'
$replacement2 = "`$1`n$connectionFactoryConfig"

$newContent = $newContent -replace $pattern2, $replacement2

# Buscar la línea que contiene "jms-queue" y agregar después
$pattern3 = '(<jms-queue name="ExpiryQueue" entries="java:/jms/queue/ExpiryQueue"/>)'
$replacement3 = "`$1`n$queueConfig"

$newContent = $newContent -replace $pattern3, $replacement3

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "✅ Conector de ActiveMQ agregado!"
Write-Host "✅ Connection factory externo agregado!"
Write-Host "✅ Cola externa agregada!"
Write-Host ""
Write-Host "Configuración completa:"
Write-Host "- Socket binding: activemq-socket-binding (puerto 61616)"
Write-Host "- Connector: activemq-connector"
Write-Host "- Connection Factory: activemq-external"
Write-Host "- Queue: excel-input-queue"
Write-Host ""
Write-Host "Ahora puedes reiniciar WildFly para aplicar los cambios."
