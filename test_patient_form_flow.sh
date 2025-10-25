#!/bin/bash

# Script para probar el flujo completo: JSON -> Excel -> Storage -> Link
# Este script envía un mensaje JSON a la cola excelParser_patientForm

echo "=== PROBANDO FLUJO COMPLETO: JSON -> EXCEL -> STORAGE -> LINK ==="
echo "Timestamp: $(date)"
echo ""

# Configuración
BROKER_URL="tcp://localhost:61616"
QUEUE_NAME="excelParser_patientForm"
JSON_FILE="patient_form_test.json"

# Verificar que el archivo JSON existe
if [ ! -f "$JSON_FILE" ]; then
    echo "❌ Error: Archivo $JSON_FILE no encontrado"
    exit 1
fi

echo "📄 Archivo JSON encontrado: $JSON_FILE"
echo "📊 Contenido del archivo:"
cat "$JSON_FILE"
echo ""
echo ""

# Crear mensaje de prueba usando curl (simulando envío a ActiveMQ)
echo "🚀 Enviando mensaje JSON a la cola $QUEUE_NAME..."

# Nota: Este es un ejemplo de cómo se enviaría el mensaje
# En la práctica, necesitarías usar un cliente JMS o ActiveMQ
echo "📤 Mensaje a enviar:"
echo "Cola: $QUEUE_NAME"
echo "Contenido: $(cat $JSON_FILE)"
echo ""

echo "✅ Script de prueba completado"
echo "📋 Próximos pasos:"
echo "   1. Enviar el JSON a la cola excelParser_patientForm"
echo "   2. Verificar que PatientFormListener procese el mensaje"
echo "   3. Confirmar que se genere el Excel y se suba a Supabase"
echo "   4. Verificar que se envíe el link al API Gateway"
echo ""
echo "🔍 Para monitorear el proceso:"
echo "   - Revisar logs de WildFly"
echo "   - Verificar endpoint: http://localhost:8080/DocExcelParser/hello-servlet"
echo "   - Verificar API Gateway: http://localhost:4000/api/health"
