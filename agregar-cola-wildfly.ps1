# Script para agregar cola al broker interno de WildFly
Write-Host "Agregando cola excel.input.queue al broker interno de WildFly..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"

# Leer todo el contenido del archivo
$content = Get-Content $configFile -Raw

# Configuración de la cola
$queueConfig = @"
                <jms-queue name="excel-input-queue" entries="java:/jms/queue/excel-input-queue"/>
"@

# Buscar específicamente después del pooled-connection-factory
$pattern = '(</pooled-connection-factory>)'
$replacement = "`$1`n$queueConfig"

$newContent = $content -replace $pattern, $replacement

# Guardar el archivo modificado
$newContent | Out-File $configFile -Encoding UTF8 -NoNewline

Write-Host "Cola excel.input.queue agregada al broker interno de WildFly!"
Write-Host "Configuracion completa lista!"
