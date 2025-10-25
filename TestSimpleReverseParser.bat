@echo off
echo ==========================================
echo === PRUEBA SIMPLE: REVERSE PARSER ===
echo ==========================================
echo Timestamp: %date% %time%
echo.

echo Enviando mensaje JSON a la cola excelParser_patientForm...
echo.

curl -X POST ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Basic YWRtaW46YWRtaW4=" ^
  -d @patient_form_test.json ^
  "http://localhost:8161/api/message?destination=queue://excelParser_patientForm&type=queue"

if %errorlevel% equ 0 (
    echo.
    echo ✅ Mensaje enviado exitosamente
    echo.
    echo 📋 Ahora revisa los logs de WildFly para ver:
    echo    1. Si APIGatewayPublisherService se inicializa correctamente
    echo    2. Si se procesa el mensaje JSON
    echo    3. Si se genera el Excel
    echo    4. Si se sube a Supabase Storage
    echo    5. Si se envía el link a la cola excel-generated-links
    echo.
    echo 📋 Logs esperados:
    echo    === INICIALIZANDO APIGatewayPublisherService ===
    echo    ✅ APIGatewayPublisherService inicializado correctamente
    echo    === PATIENT FORM MESSAGE RECEIVED ===
    echo    === PROCESANDO FORMULARIO DE PACIENTE ===
    echo    ✅ Excel generado: XXXX bytes
    echo    ✅ Excel subido a Supabase Storage: generated/XXXX.xlsx
    echo    ✅ URL de descarga generada: https://...
    echo    === ENVIANDO LINK DE EXCEL A LA COLA PARA API GATEWAY ===
    echo    📤 Enviando mensaje JMS...
    echo    ✅ LINK DE EXCEL ENVIADO EXITOSAMENTE A LA COLA
    echo.
    echo 📋 Después de ver los logs, verifica en ActiveMQ Admin:
    echo    1. Ve a http://localhost:8161/admin
    echo    2. Login con admin/admin
    echo    3. Ve a "Queues"
    echo    4. Busca "excel-generated-links"
    echo    5. Debería mostrar 1 mensaje
) else (
    echo.
    echo ❌ Error enviando mensaje
)

echo.
echo ==========================================
echo === PRUEBA COMPLETADA ===
echo ==========================================
pause
