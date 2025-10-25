@echo off
echo ==========================================
echo === PRUEBA DIRECTA: CREAR COLA ===
echo ==========================================
echo.

echo Enviando mensaje de prueba directamente a la cola excel-generated-links...
echo Esto deberia crear la cola si no existe.
echo.

curl -X POST ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Basic YWRtaW46YWRtaW4=" ^
  -d "{\"test\":\"message\",\"downloadUrl\":\"https://test.com/file.xlsx\",\"fileName\":\"test.xlsx\",\"messageId\":\"test-123\",\"timestamp\":\"2025-01-24T00:00:00Z\",\"source\":\"test\",\"status\":\"test\"}" ^
  "http://localhost:8161/api/message?destination=queue://excel-generated-links&type=queue"

if %errorlevel% equ 0 (
    echo.
    echo ✅ Mensaje de prueba enviado exitosamente
    echo.
    echo 📋 Ahora verifica en ActiveMQ Admin:
    echo    1. Ve a http://localhost:8161/admin
    echo    2. Login con admin/admin
    echo    3. Ve a "Queues"
    echo    4. Busca "excel-generated-links"
    echo    5. Deberia mostrar 1 mensaje de prueba
    echo.
    echo 📋 Si la cola se creo correctamente, entonces el problema
    echo    esta en el APIGatewayPublisherService de WildFly
) else (
    echo.
    echo ❌ Error enviando mensaje de prueba
)

echo.
echo ==========================================
echo === PRUEBA COMPLETADA ===
echo ==========================================
pause
