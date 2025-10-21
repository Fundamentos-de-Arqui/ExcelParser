# Configuración JMS para WildFly
# Instrucciones para configurar la conexión JMS con ActiveMQ

# 1. Configurar ActiveMQ en WildFly
# Agregar al archivo standalone.xml de WildFly:

# <subsystem xmlns="urn:jboss:domain:messaging-activemq:1.0">
#     <server name="default">
#         <security-setting name="#">
#             <role name="guest" send="true" consume="true" create-durable-queue="true" delete-durable-queue="true" create-non-durable-queue="true" delete-non-durable-queue="true"/>
#         </security-setting>
#         
#         <jms-queue name="excel.input.queue" entries="queue/excel.input.queue java:jboss/exported/jms/queue/excel.input.queue"/>
#         
#         <connection-factory name="InVmConnectionFactory" entries="java:/ConnectionFactory" connectors="in-vm"/>
#         <connection-factory name="RemoteConnectionFactory" entries="java:jboss/exported/jms/RemoteConnectionFactory" connectors="http-connector"/>
#         
#         <pooled-connection-factory name="activemq-ra" entries="java:/JmsXA java:jboss/DefaultJMSConnectionFactory" connectors="in-vm" transaction="xa"/>
#     </server>
# </subsystem>

# 2. Alternativa: Usar JNDI para conexión externa
# El WebListener puede conectarse directamente a ActiveMQ externo usando JNDI

# 3. Configuración del WebListener
# El WebListener está configurado para leer de la cola "excel.input.queue"
# y almacenar mensajes para consulta posterior

# 4. Verificar configuración
# - Asegurar que ActiveMQ esté corriendo en localhost:61616
# - Verificar que la cola "excel.input.queue" exista
# - El WebListener se conectará automáticamente al iniciar la aplicación
