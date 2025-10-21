@echo off
echo ==========================================
echo === ENVIANDO MENSAJE DE PRUEBA ===
echo === COLA: excel.input.queue ===
echo ==========================================

REM Configuración
set BROKER_URL=http://localhost:8161/api/message
set QUEUE_NAME=excel.input.queue
set USERNAME=admin
set PASSWORD=admin

REM Crear mensaje JSON simple
set TIMESTAMP=%date:~-4,4%-%date:~-10,2%-%date:~-7,2%T%time:~0,2%:%time:~3,2%:%time:~6,2%
set MESSAGE_ID=test-%date:~-4,4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%

echo Mensaje JSON a enviar:
echo {"messageId":"%MESSAGE_ID%","fileName":"mensaje_prueba.json","status":"TEST","receivedAt":"%TIMESTAMP%","excelBase64":"SG9sYSBNdW5kbyAtIE1lbnNhamUgZGUgUHJ1ZWJh","testData":"Este es un mensaje de prueba para verificar el polling"}

echo.
echo Enviando mensaje...

REM Crear archivo temporal con el mensaje JSON
echo {"messageId":"%MESSAGE_ID%","fileName":"mensaje_prueba.json","status":"TEST","receivedAt":"%TIMESTAMP%","excelBase64":"SG9sYSBNdW5kbyAtIE1lbnNhamUgZGUgUHJ1ZWJh","testData":"Este es un mensaje de prueba para verificar el polling"} > mensaje_temp.json

REM Enviar usando curl
curl -X POST "%BROKER_URL%?destination=queue://%QUEUE_NAME%&type=queue" ^
     -H "Content-Type: application/json" ^
     -H "Authorization: Basic YWRtaW46YWRtaW4=" ^
     -d @mensaje_temp.json

echo.
echo ==========================================
echo === MENSAJE ENVIADO ===
echo === TIMESTAMP: %date% %time% ===
echo ==========================================

REM Limpiar archivo temporal
del mensaje_temp.json

echo.
echo Verifica ahora el polling en la aplicación web...
echo URL: http://localhost:8080/DocExcelParser-1.0-SNAPSHOT/hello-servlet
pause
