@echo off
echo ==========================================
echo === VERIFICACION DE COLAS JMS ===
echo ==========================================
echo.

echo Verificando colas en ActiveMQ...
echo.

echo 1. Verificando cola excelParser_patientForm...
curl -s -H "Authorization: Basic YWRtaW46YWRtaW4=" "http://localhost:8161/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=excelParser_patientForm"

echo.
echo.

echo 2. Verificando cola excel-generated-links...
curl -s -H "Authorization: Basic YWRtaW46YWRtaW4=" "http://localhost:8161/api/jolokia/read/org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=excel-generated-links"

echo.
echo.

echo 3. Listando todas las colas...
curl -s -H "Authorization: Basic YWRtaW46YWRtaW4=" "http://localhost:8161/api/jolokia/exec/org.apache.activemq:type=Broker,brokerName=localhost/getQueueNames"

echo.
echo.

echo ==========================================
echo === INSTRUCCIONES ===
echo ==========================================
echo.
echo Para verificar manualmente:
echo 1. Ve a http://localhost:8161/admin
echo 2. Login con admin/admin
echo 3. Ve a "Queues" en el menu
echo 4. Busca las colas:
echo    - excelParser_patientForm (deberia existir)
echo    - excel-generated-links (deberia crearse cuando se procese un mensaje)
echo.

pause
