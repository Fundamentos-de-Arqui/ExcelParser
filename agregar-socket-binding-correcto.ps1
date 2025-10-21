# Script para agregar configuración de ActiveMQ en el lugar correcto
Write-Host "Agregando configuración de ActiveMQ en el lugar correcto..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración de ActiveMQ que necesitamos agregar
$activemqConfig = @"
        <socket-binding name="activemq-socket-binding" port="61616"/>
"@

# Buscar la línea que contiene "txn-status-manager" y agregar después
$pattern = '(<socket-binding name="txn-status-manager" port="4713"/>)'
$replacement = "`$1`n$activemqConfig"

$newContent = $content -replace $pattern, $replacement

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "✅ Socket binding de ActiveMQ agregado correctamente!"
Write-Host "✅ Puerto: 61616"
Write-Host ""
Write-Host "Ahora necesitamos agregar la configuración del conector y connection factory."
Write-Host "Esto se debe hacer en la sección de messaging-activemq."
