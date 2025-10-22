# Script para enviar mensaje de prueba al MQ
# Envía un mensaje JSON a ActiveMQ para probar el listener

Write-Host "==========================================" -ForegroundColor Blue
Write-Host "=== MQ MESSAGE SENDER TEST ===" -ForegroundColor Blue
Write-Host "=== Enviando mensaje de prueba... ===" -ForegroundColor Blue
Write-Host "==========================================" -ForegroundColor Blue

# Compilar el proyecto
Write-Host "Compilando proyecto..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilación exitosa. Enviando mensaje..." -ForegroundColor Green
    
    # Ejecutar el sender
    mvn exec:java -Dexec.mainClass="com.soulware.platform.mqtest.MQMessageSenderTest"
} else {
    Write-Host "Error en la compilación. Revisa los errores." -ForegroundColor Red
}
