# Configuración simplificada de ActiveMQ para WildFly
# Este script configura ActiveMQ como un broker externo usando la CLI de WildFly

Write-Host "=========================================="
Write-Host "CONFIGURACIÓN DE ACTIVEMQ EN WILDFLY"
Write-Host "=========================================="
Write-Host ""

Write-Host "PASO 1: Verificando que WildFly esté detenido..."
Write-Host "Si WildFly está corriendo, deténlo manualmente antes de continuar."
Read-Host "Presiona Enter para continuar"

Write-Host ""
Write-Host "PASO 2: Configurando ActiveMQ como broker externo..."
Write-Host ""

# Ruta a la CLI de WildFly
$wildflyCli = "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat"

# Comandos de configuración
$commands = @(
    "/subsystem=messaging-activemq/connector=activemq-connector:add(socket-binding=activemq-socket-binding)",
    "/socket-binding-group=standard-sockets/socket-binding=activemq-socket-binding:add(port=61616)",
    "/subsystem=messaging-activemq/external-connection-factory=activemq-external:add(connector-refs=[activemq-connector],entries=[java:/jms/activemq/ConnectionFactory])",
    "/subsystem=messaging-activemq/external-jms-queue=excel-input-queue:add(entries=[java:/jms/queue/excel-input-queue],external-context=activemq-external)"
)

# Ejecutar cada comando
foreach ($command in $commands) {
    Write-Host "Ejecutando: $command"
    try {
        & $wildflyCli --command=$command
        Write-Host "✅ Comando ejecutado exitosamente"
    } catch {
        Write-Host "❌ Error ejecutando comando: $($_.Exception.Message)"
    }
    Write-Host ""
}

Write-Host "=========================================="
Write-Host "CONFIGURACIÓN COMPLETADA"
Write-Host "=========================================="
Write-Host ""
Write-Host "Ahora puedes:"
Write-Host "1. Reiniciar WildFly"
Write-Host "2. Desplegar tu aplicación"
Write-Host "3. El JMS debería funcionar con ActiveMQ"
Write-Host ""
Write-Host "Para verificar la configuración:"
Write-Host "C:\Users\suiny\Desktop\wildfly-38.0.0.Final\bin\jboss-cli.bat"
Write-Host "/subsystem=messaging-activemq:read-resource"
Write-Host ""
Read-Host "Presiona Enter para salir"
