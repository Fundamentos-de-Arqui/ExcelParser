# Script para enviar mensaje JSON a ActiveMQ usando PowerShell
$messageJson = @"
{
  "messageId": "test-from-powershell-" + $(Get-Date -Format "yyyyMMddHHmmss"),
  "fileName": "test-from-powershell.xlsx",
  "status": "POWERSHELL_TEST",
  "receivedAt": "$(Get-Date -Format "yyyy-MM-ddTHH:mm:ss")",
  "excelBase64": "VGVzdCBNZXNzYWdlIGZyb20gUG93ZXJTaGVsbA=="
}
"@

Write-Host "=========================================="
Write-Host "=== ENVIANDO MENSAJE JSON A ACTIVEMQ ==="
Write-Host "=========================================="
Write-Host "Mensaje JSON:"
Write-Host $messageJson
Write-Host "=========================================="

# Enviar mensaje usando Invoke-RestMethod
try {
    $headers = @{
        "Content-Type" = "application/json"
        "Authorization" = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin"))
    }
    
    $uri = "http://localhost:8161/api/message/excel-input-queue?type=queue"
    
    $response = Invoke-RestMethod -Uri $uri -Method POST -Body $messageJson -Headers $headers
    
    Write-Host "=========================================="
    Write-Host "=== MENSAJE ENVIADO EXITOSAMENTE ==="
    Write-Host "Respuesta: $response"
    Write-Host "=========================================="
    
} catch {
    Write-Host "=========================================="
    Write-Host "=== ERROR ENVIANDO MENSAJE ==="
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host "=========================================="
}
