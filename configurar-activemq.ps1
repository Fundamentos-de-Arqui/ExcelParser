# Script para configurar ActiveMQ en WildFly
# Este script configura ActiveMQ como un broker externo

# Detener WildFly si está corriendo
echo "Deteniendo WildFly..."
# Aquí deberías detener WildFly manualmente si está corriendo

# Configurar ActiveMQ como broker externo
echo "Configurando ActiveMQ como broker externo..."

# Crear el archivo de configuración para ActiveMQ
$configContent = @"
# Configuración de ActiveMQ para WildFly
# Este archivo se debe aplicar usando la CLI de WildFly

# 1. Configurar el broker externo de ActiveMQ
/subsystem=messaging-activemq/external-connection-factory=activemq-external:add(
    connector-refs=[activemq-connector],
    entries=[java:/jms/activemq/ConnectionFactory]
)

# 2. Configurar el conector para ActiveMQ
/subsystem=messaging-activemq/connector=activemq-connector:add(
    socket-binding=activemq-socket-binding
)

# 3. Configurar el socket binding para ActiveMQ
/socket-binding-group=standard-sockets/socket-binding=activemq-socket-binding:add(
    port=61616
)

# 4. Configurar la cola de destino
/subsystem=messaging-activemq/external-jms-queue=excel-input-queue:add(
    entries=[java:/jms/queue/excel-input-queue],
    external-context=activemq-external
)

# 5. Configurar el topic de destino (opcional)
/subsystem=messaging-activemq/external-jms-topic=excel-input-topic:add(
    entries=[java:/jms/topic/excel-input-topic],
    external-context=activemq-external
)
"@

# Guardar la configuración en un archivo
$configContent | Out-File -FilePath "activemq-config.cli" -Encoding UTF8

echo "Archivo de configuración creado: activemq-config.cli"
echo ""
echo "Para aplicar la configuración:"
echo "1. Detén WildFly"
echo "2. Ejecuta: C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat --file=activemq-config.cli"
echo "3. Reinicia WildFly"
echo ""
echo "O ejecuta cada comando manualmente en la CLI de WildFly:"
echo "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat"
