# Script para ejecutar el MQ Listener Test
# Ejecuta el listener que escucha mensajes de ActiveMQ

Write-Host "==========================================" -ForegroundColor Green
Write-Host "=== MQ LISTENER TEST ===" -ForegroundColor Green
Write-Host "=== Ejecutando listener JMS... ===" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

# Compilar el proyecto
Write-Host "Compilando proyecto..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilación exitosa. Ejecutando listener..." -ForegroundColor Green
    
    # Ejecutar el listener
    mvn exec:java -Dexec.mainClass="com.soulware.platform.mqtest.MQListenerTest"
} else {
    Write-Host "Error en la compilación. Revisa los errores." -ForegroundColor Red
}
