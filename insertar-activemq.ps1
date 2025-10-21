# Script para insertar configuración de ActiveMQ en standalone.xml
Write-Host "Configurando ActiveMQ en WildFly..."

$configFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml"
$backupFile = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\standalone\configuration\standalone.xml.backup-jms"

# Crear backup
Copy-Item $configFile $backupFile -Force
Write-Host "Backup creado: $backupFile"

# Leer el archivo
$lines = Get-Content $configFile

# Crear nuevo contenido con la configuración de ActiveMQ
$newLines = @()
$inserted = $false

foreach ($line in $lines) {
    $newLines += $line
    
    # Insertar después de la línea que contiene "txn-status-manager"
    if ($line -match "txn-status-manager" -and -not $inserted) {
        $newLines += '        <socket-binding name="activemq-socket-binding" port="61616"/>'
        $inserted = $true
        Write-Host "Socket binding agregado"
    }
}

# Guardar el archivo modificado
$newLines | Out-File $configFile -Encoding UTF8
Write-Host "Configuración completada!"
Write-Host "Reinicia WildFly para aplicar los cambios."
