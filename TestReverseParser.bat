@echo off
echo === PROBANDO REVERSE PARSER: JSON -^> EXCEL -^> STORAGE -^> LINK ===
echo Timestamp: %date% %time%
echo.

echo Verificando archivo JSON...
if not exist "patient_form_test.json" (
    echo Error: Archivo patient_form_test.json no encontrado
    exit /b 1
)

echo Archivo JSON encontrado exitosamente
echo.

echo Enviando mensaje JSON a la cola excelParser_patientForm...
echo URL: http://localhost:8161/api/message
echo Cola: excelParser_patientForm
echo.

curl -X POST ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Basic YWRtaW46YWRtaW4=" ^
  -d @patient_form_test.json ^
  "http://localhost:8161/api/message?destination=queue://excelParser_patientForm&type=queue"

if %errorlevel% equ 0 (
    echo.
    echo Mensaje enviado exitosamente a la cola excelParser_patientForm
    echo.
    echo Monitoreando resultados...
    echo.
    echo Logs esperados en WildFly:
    echo    === PATIENT FORM MESSAGE RECEIVED ===
    echo    === PROCESANDO FORMULARIO DE PACIENTE ===
    echo    Excel generado: XXXX bytes
    echo    Excel subido a Supabase Storage: generated/XXXX.xlsx
    echo    URL de descarga generada: https://...
    echo    URL enviada al API Gateway exitosamente
    echo.
    echo Verificaciones a realizar:
    echo 1. Revisa los logs de WildFly para ver el procesamiento
    echo 2. Verifica en Supabase Storage que se subio el Excel
    echo 3. Verifica en la cola excel-generated-links que llego el link
    echo.
    echo URLs utiles:
    echo    - Monitor WildFly: http://localhost:8080/DocExcelParser/hello-servlet
    echo    - Supabase Storage: https://ecjzscyihpidhjbkuimh.storage.supabase.co/storage/v1/s3/my-bucket
) else (
    echo.
    echo Error enviando mensaje a la cola
    echo.
    echo Posibles soluciones:
    echo 1. Verifica que ActiveMQ este ejecutandose en localhost:8161
    echo 2. Verifica que las credenciales sean correctas (admin/admin)
    echo 3. Verifica que la cola excelParser_patientForm exista
)

echo.
echo === SCRIPT COMPLETADO ===
pause
