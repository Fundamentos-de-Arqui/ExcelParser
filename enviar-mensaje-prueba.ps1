# Script para enviar mensaje de prueba a ActiveMQ
# Este script envía un mensaje JSON simple a la cola excel.input.queue

Write-Host "=========================================="
Write-Host "=== ENVIANDO MENSAJE DE PRUEBA ==="
Write-Host "=== COLA: excel.input.queue ==="
Write-Host "=========================================="

# Configuración
$brokerUrl = "http://localhost:8161/api/message"
$queueName = "excel.input.queue"
$username = "admin"
$password = "admin"

# Crear mensaje JSON simple
$testMessage = @{
    messageId = "test-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    fileName = "mensaje_prueba.json"
    status = "TEST"
    receivedAt = (Get-Date -Format "yyyy-MM-ddTHH:mm:ss")
    excelBase64 = "SG9sYSBNdW5kbyAtIE1lbnNhamUgZGUgUHJ1ZWJh"
    testData = "Este es un mensaje de prueba para verificar el polling"
} | ConvertTo-Json -Depth 3

Write-Host "Mensaje JSON a enviar:"
Write-Host $testMessage
Write-Host ""

# Crear credenciales base64
$credentials = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${username}:${password}"))

# Configurar headers
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Basic $credentials"
}

# URL completa con parámetros
$fullUrl = "${brokerUrl}?destination=queue://${queueName}&type=queue"

Write-Host "URL: $fullUrl"
Write-Host "Enviando mensaje..."

try {
    # Enviar mensaje
    $response = Invoke-RestMethod -Uri $fullUrl -Method POST -Body $testMessage -Headers $headers
    
    Write-Host "=========================================="
    Write-Host "=== MENSAJE ENVIADO EXITOSAMENTE ==="
    Write-Host "=== TIMESTAMP: $(Get-Date) ==="
    Write-Host "=========================================="
    
} catch {
    Write-Host "=========================================="
    Write-Host "=== ERROR ENVIANDO MENSAJE ==="
    Write-Host "=== ERROR: $($_.Exception.Message) ==="
    Write-Host "=========================================="
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "Código de estado: $statusCode"
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Cuerpo del error: $errorBody"
        } catch {
            Write-Host "No se pudo leer el cuerpo del error"
        }
    }
}

Write-Host ""
Write-Host "Verifica ahora el polling en la aplicación web..."
Write-Host "URL: http://localhost:8080/DocExcelParser-1.0-SNAPSHOT/hello-servlet"
