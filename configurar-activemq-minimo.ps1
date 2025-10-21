# Script para configuración mínima de ActiveMQ
Write-Host "Configuracion minima de ActiveMQ..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

Write-Host "Agregando socket binding..."
$socketBindingConfig = @"
        <socket-binding name="activemq-socket-binding" port="61616"/>
"@
$pattern1 = '(<socket-binding name="txn-status-manager" port="4713"/>)'
$replacement1 = "`$1`n$socketBindingConfig"
$newContent = $content -replace $pattern1, $replacement1

Write-Host "Agregando conector..."
$connectorConfig = @"
                <connector name="activemq-connector" socket-binding="activemq-socket-binding"/>
"@
$pattern2 = '(</in-vm-connector>)'
$replacement2 = "`$1`n$connectorConfig"
$newContent = $newContent -replace $pattern2, $replacement2

Write-Host "Agregando connection factory externo..."
$connectionFactoryConfig = @"
                <external-connection-factory name="activemq-external" connector-refs="activemq-connector" entries="java:/jms/activemq/ConnectionFactory"/>
"@
$pattern3 = '(<connection-factory name="RemoteConnectionFactory" entries="java:jboss/exported/jms/RemoteConnectionFactory" connectors="http-connector"/>)'
$replacement3 = "`$1`n$connectionFactoryConfig"
$newContent = $newContent -replace $pattern3, $replacement3

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "Configuracion minima de ActiveMQ completada!"
Write-Host "Socket binding, conector y connection factory agregados."
