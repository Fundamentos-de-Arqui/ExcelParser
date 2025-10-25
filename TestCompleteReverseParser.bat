@echo off
echo ==========================================
echo === PRUEBA COMPLETA: REVERSE PARSER ===
echo === JSON -^> EXCEL -^> STORAGE -^> JMS ===
echo ==========================================
echo Timestamp: %date% %time%
echo.

echo Verificando componentes del sistema...
echo.

echo 1. Verificando archivo JSON de prueba...
if not exist "patient_form_test.json" (
    echo ❌ Error: Archivo patient_form_test.json no encontrado
    exit /b 1
)
echo ✅ Archivo JSON encontrado
echo.

echo 2. Verificando conexion a ActiveMQ...
curl -s -o nul -w "Status: %%{http_code}" http://localhost:8161/api/message
if %errorlevel% neq 0 (
    echo ❌ Error: ActiveMQ no responde en localhost:8161
    echo    Asegurate de que ActiveMQ este ejecutandose
    exit /b 1
)
echo ✅ ActiveMQ responde correctamente
echo.

echo 3. Verificando aplicacion WildFly...
curl -s -o nul -w "Status: %%{http_code}" http://localhost:8080/DocExcelParser/hello-servlet
if %errorlevel% neq 0 (
    echo ❌ Error: WildFly no responde en localhost:8080
    echo    Asegurate de que WildFly este ejecutandose con DocExcelParser deployado
    exit /b 1
)
echo ✅ WildFly responde correctamente
echo.

echo 4. Verificando API Gateway...
curl -s -o nul -w "Status: %%{http_code}" http://localhost:4000/api/health
if %errorlevel% neq 0 (
    echo ⚠️  Advertencia: API Gateway no responde en localhost:4000
    echo    El API Gateway debe estar ejecutandose para consumir de la cola
    echo    Continuando con la prueba...
) else (
    echo ✅ API Gateway responde correctamente
)
echo.

echo ==========================================
echo === INICIANDO PRUEBA DEL REVERSE PARSER ===
echo ==========================================
echo.

echo Enviando mensaje JSON a la cola excelParser_patientForm...
echo Cola destino: excelParser_patientForm
echo.

curl -X POST ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Basic YWRtaW46YWRtaW4=" ^
  -d @patient_form_test.json ^
  "http://localhost:8161/api/message?destination=queue://excelParser_patientForm&type=queue"

if %errorlevel% equ 0 (
    echo.
    echo ✅ Mensaje enviado exitosamente a la cola excelParser_patientForm
    echo.
    echo ==========================================
    echo === RESULTADOS ESPERADOS ===
    echo ==========================================
    echo.
    echo 📋 Logs esperados en WildFly:
    echo    === PATIENT FORM MESSAGE RECEIVED ===
    echo    === PROCESANDO FORMULARIO DE PACIENTE ===
    echo    ✅ Excel generado: XXXX bytes
    echo    ✅ Excel subido a Supabase Storage: generated/XXXX.xlsx
    echo    ✅ URL de descarga generada: https://...
    echo    === ENVIANDO LINK DE EXCEL A LA COLA PARA API GATEWAY ===
    echo    ✅ LINK DE EXCEL ENVIADO EXITOSAMENTE A LA COLA
    echo.
    echo 📋 Verificaciones a realizar:
    echo    1. Revisa los logs de WildFly para ver el procesamiento
    echo    2. Verifica en Supabase Storage que se subio el Excel
    echo    3. Verifica en ActiveMQ que se creo la cola excel-generated-links
    echo    4. Verifica en el API Gateway que recibio el mensaje de la cola
    echo.
    echo 📋 URLs utiles:
    echo    - Monitor WildFly: http://localhost:8080/DocExcelParser/hello-servlet
    echo    - Supabase Storage: https://ecjzscyihpidhjbkuimh.storage.supabase.co/storage/v1/s3/my-bucket
    echo    - ActiveMQ Admin: http://localhost:8161/admin
    echo    - API Gateway Health: http://localhost:4000/api/health
    echo    - API Gateway Consumer Status: http://localhost:4000/api/excel/consumer-status
    echo.
    echo 📋 Para verificar la cola excel-generated-links:
    echo    1. Ve a http://localhost:8161/admin
    echo    2. Login con admin/admin
    echo    3. Ve a "Queues" en el menu
    echo    4. Busca la cola "excel-generated-links"
    echo    5. Deberia mostrar 1 mensaje con el link del Excel
    echo.
) else (
    echo.
    echo ❌ Error enviando mensaje a la cola
    echo.
    echo Posibles soluciones:
    echo 1. Verifica que ActiveMQ este ejecutandose en localhost:8161
    echo 2. Verifica que las credenciales sean correctas (admin/admin)
    echo 3. Verifica que la cola excelParser_patientForm exista
)

echo.
echo ==========================================
echo === PRUEBA COMPLETADA ===
echo ==========================================
pause
