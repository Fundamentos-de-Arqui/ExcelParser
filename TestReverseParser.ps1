# TestReverseParser.ps1
Write-Host "=== PROBANDO REVERSE PARSER: JSON -> EXCEL -> STORAGE -> LINK ===" -ForegroundColor Green
Write-Host "Timestamp: $(Get-Date)" -ForegroundColor Yellow
Write-Host ""

# Verificar que el archivo JSON existe
if (-not (Test-Path "patient_form_test.json")) {
    Write-Host "❌ Error: Archivo patient_form_test.json no encontrado" -ForegroundColor Red
    Write-Host "Asegúrate de que el archivo esté en el directorio actual" -ForegroundColor Yellow
    exit 1
}

# Leer archivo JSON
try {
    $jsonContent = Get-Content "patient_form_test.json" -Raw -Encoding UTF8
    Write-Host "📄 Archivo JSON encontrado y leído exitosamente" -ForegroundColor Cyan
    Write-Host "📊 Contenido del archivo:" -ForegroundColor Cyan
    Write-Host $jsonContent -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "❌ Error leyendo archivo JSON: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Configuración
$activemqUrl = "http://localhost:8161/api/message"
$queueName = "excelParser_patientForm"

Write-Host "🚀 Enviando mensaje JSON a la cola $queueName..." -ForegroundColor Yellow
Write-Host "URL: $activemqUrl" -ForegroundColor Gray
Write-Host "Cola: $queueName" -ForegroundColor Gray
Write-Host ""

try {
    # Enviar mensaje usando ActiveMQ REST API
    $response = Invoke-RestMethod -Uri "$activemqUrl?destination=queue://$queueName&type=queue" `
        -Method POST `
        -ContentType "application/json" `
        -Headers @{Authorization="Basic YWRtaW46YWRtaW4="} `
        -Body $jsonContent `
        -TimeoutSec 30
    
    Write-Host "✅ Mensaje enviado exitosamente a la cola $queueName" -ForegroundColor Green
    Write-Host ""
    Write-Host "🔍 Monitoreando resultados..." -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📋 Logs esperados en WildFly:" -ForegroundColor Yellow
    Write-Host "   === PATIENT FORM MESSAGE RECEIVED ===" -ForegroundColor White
    Write-Host "   === PROCESANDO FORMULARIO DE PACIENTE ===" -ForegroundColor White
    Write-Host "   ✅ Excel generado: XXXX bytes" -ForegroundColor Green
    Write-Host "   ✅ Excel subido a Supabase Storage: generated/XXXX.xlsx" -ForegroundColor Green
    Write-Host "   ✅ URL de descarga generada: https://..." -ForegroundColor Green
    Write-Host "   ✅ URL enviada al API Gateway exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "🎯 Verificaciones a realizar:" -ForegroundColor Yellow
    Write-Host "1. Revisa los logs de WildFly para ver el procesamiento" -ForegroundColor White
    Write-Host "2. Verifica en Supabase Storage que se subió el Excel" -ForegroundColor White
    Write-Host "3. Verifica en la cola excel-generated-links que llegó el link" -ForegroundColor White
    Write-Host ""
    Write-Host "🌐 URLs útiles:" -ForegroundColor Yellow
    Write-Host "   - Monitor WildFly: http://localhost:8080/DocExcelParser/hello-servlet" -ForegroundColor Cyan
    Write-Host "   - Supabase Storage: https://ecjzscyihpidhjbkuimh.storage.supabase.co/storage/v1/s3/my-bucket" -ForegroundColor Cyan
    Write-Host ""
    
} catch {
    Write-Host "❌ Error enviando mensaje: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔧 Posibles soluciones:" -ForegroundColor Yellow
    Write-Host "1. Verifica que ActiveMQ esté ejecutándose en localhost:8161" -ForegroundColor White
    Write-Host "2. Verifica que las credenciales sean correctas (admin/admin)" -ForegroundColor White
    Write-Host "3. Verifica que la cola excelParser_patientForm exista" -ForegroundColor White
    Write-Host ""
    Write-Host "💡 Para verificar ActiveMQ:" -ForegroundColor Yellow
    Write-Host "   curl http://localhost:8161/api/message" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "=== SCRIPT COMPLETADO ===" -ForegroundColor Green
