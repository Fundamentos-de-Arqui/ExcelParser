@echo off
echo ==========================================
echo === PRUEBA CORREGIDA: REVERSE PARSER ===
echo ==========================================
echo Timestamp: %date% %time%
echo.

echo 🔧 CORRECCION APLICADA:
echo    - APIGatewayPublisherService ahora usa inicialización lazy
echo    - La conexión JMS se inicializa cuando se necesita
echo    - Esto debería resolver el error de ConnectionFactory
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
    echo 📋 Logs esperados en WildFly (CORREGIDOS):
    echo    === APIGatewayPublisherService CONSTRUCTOR ===
    echo    ✅ APIGatewayPublisherService creado (inicialización lazy)
    echo    === PATIENT FORM MESSAGE RECEIVED ===
    echo    === PROCESANDO FORMULARIO DE PACIENTE ===
    echo    ✅ Excel generado: XXXX bytes
    echo    ✅ Excel subido a Supabase Storage: generated/XXXX.xlsx
    echo    ✅ URL de descarga generada: https://...
    echo    === ENVIANDO LINK DE EXCEL A LA COLA PARA API GATEWAY ===
    echo    🔄 Inicializando conexión JMS (lazy)...
    echo    === INICIALIZANDO APIGatewayPublisherService (LAZY) ===
    echo    ✅ APIGatewayPublisherService inicializado correctamente
    echo    📤 Enviando mensaje JMS...
    echo    ✅ LINK DE EXCEL ENVIADO EXITOSAMENTE A LA COLA
    echo.
    echo 📋 Verificaciones:
    echo    1. Revisa los logs de WildFly para confirmar la corrección
    echo    2. Verifica en ActiveMQ Admin que se creó la cola excel-generated-links
    echo    3. Verifica que hay 1 mensaje en la cola excel-generated-links
    echo.
    echo 📋 URLs:
    echo    - ActiveMQ Admin: http://localhost:8161/admin
    echo    - Supabase Storage: https://ecjzscyihpidhjbkuimh.storage.supabase.co/storage/v1/s3/my-bucket
    echo.
) else (
    echo.
    echo ❌ Error enviando mensaje
)

echo.
echo ==========================================
echo === PRUEBA COMPLETADA ===
echo ==========================================
pause
