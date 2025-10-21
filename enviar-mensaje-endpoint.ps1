# Script para enviar mensaje real usando el endpoint del servlet
Write-Host "Enviando mensaje real usando endpoint del servlet..."

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

# Enviar mensaje usando el endpoint del servlet
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/DocExcelParser-1.0-SNAPSHOT/hello-servlet?action=sendMessage" -Method POST -ContentType "application/json" -Body $message
    Write-Host "Respuesta del servlet:"
    Write-Host $response
} catch {
    Write-Host "Error enviando mensaje: $($_.Exception.Message)"
}

Write-Host "Mensaje enviado! Revisa los logs de WildFly para ver si aparece el mensaje."
