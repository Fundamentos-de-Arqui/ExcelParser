# Script para agregar socket binding de ActiveMQ
Write-Host "Agregando socket binding de ActiveMQ..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración del socket binding de ActiveMQ
$socketBindingConfig = @"
        <socket-binding name="activemq-socket-binding" port="61616"/>
"@

# Buscar la línea que contiene "txn-status-manager" y agregar después
$pattern = '(<socket-binding name="txn-status-manager" port="4713"/>)'
$replacement = "`$1`n$socketBindingConfig"

$newContent = $content -replace $pattern, $replacement

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "Socket binding de ActiveMQ agregado!"
Write-Host "Configuración completa de ActiveMQ lista!"
