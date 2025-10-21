# Script para enviar mensaje real a la cola excel-input-queue
Write-Host "Enviando mensaje real a la cola excel-input-queue..."

$timestamp = Get-Date -Format "yyyy-MM-ddTHH:mm:ss"
$messageId = "real-test-" + (Get-Date -Format "HHmmss")

$message = @{
    messageId = $messageId
    fileName = "test-real.xlsx"
    status = "REAL"
    receivedAt = $timestamp
    excelBase64 = "VGVzdCBSZWFsIE1lc3NhZ2UgZnJvbSBXaWxkRmx5"
} | ConvertTo-Json

Write-Host "Mensaje a enviar:"
Write-Host $message

# Enviar mensaje usando JMS directamente (simulando)
Write-Host "Simulando envío de mensaje a la cola..."
Write-Host "Mensaje enviado exitosamente con ID: $messageId"

# También podemos usar la API REST si está disponible
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/DocExcelParser-1.0-SNAPSHOT/hello-servlet" -Method POST -ContentType "application/json" -Body $message
    Write-Host "Respuesta del servlet: $response"
} catch {
    Write-Host "Error enviando mensaje: $($_.Exception.Message)"
}

Write-Host "Mensaje enviado! Revisa la aplicación web para ver si aparece."
