@echo off
echo ==========================================
echo CONFIGURACION DE ACTIVEMQ EN WILDFLY
echo ==========================================
echo.

echo PASO 1: Verificando que WildFly este detenido...
echo Si WildFly esta corriendo, detenlo manualmente antes de continuar.
pause

echo.
echo PASO 2: Configurando ActiveMQ como broker externo...
echo.

echo Ejecutando comandos de configuracion...

REM Configurar el conector para ActiveMQ
echo Configurando conector...
C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat --command="/subsystem=messaging-activemq/connector=activemq-connector:add(socket-binding=activemq-socket-binding)"

REM Configurar el socket binding para ActiveMQ
echo Configurando socket binding...
C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat --command="/socket-binding-group=standard-sockets/socket-binding=activemq-socket-binding:add(port=61616)"

REM Configurar el broker externo de ActiveMQ
echo Configurando broker externo...
C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat --command="/subsystem=messaging-activemq/external-connection-factory=activemq-external:add(connector-refs=[activemq-connector],entries=[java:/jms/activemq/ConnectionFactory])"

REM Configurar la cola de destino
echo Configurando cola de destino...
C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat --command="/subsystem=messaging-activemq/external-jms-queue=excel-input-queue:add(entries=[java:/jms/queue/excel-input-queue],external-context=activemq-external)"

echo.
echo ==========================================
echo CONFIGURACION COMPLETADA
echo ==========================================
echo.
echo Ahora puedes:
echo 1. Reiniciar WildFly
echo 2. Desplegar tu aplicacion
echo 3. El JMS deberia funcionar con ActiveMQ
echo.
echo Para verificar la configuracion:
echo C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat
echo /subsystem=messaging-activemq:read-resource
echo.
pause
