#!/bin/bash

echo "=========================================="
echo "=== DIAGNOSTICO DE LECTURA DE COLA ==="
echo "=== REPLICA LA LOGICA DEL PROYECTO ==="
echo "=========================================="
echo ""

echo "Compilando y ejecutando DiagnoseQueueReader..."
echo ""

# Intentar con Maven
mvn compile exec:java -Dexec.mainClass="DiagnoseQueueReader" -Dexec.classpathScope=compile

if [ $? -ne 0 ]; then
    echo ""
    echo "Maven no funcionó, intentando método alternativo..."
    echo ""
    echo "Por favor, ejecuta manualmente:"
    echo "  mvn compile"
    echo "  java -cp 'target/classes:target/DocExcelParser/WEB-INF/lib/*' DiagnoseQueueReader"
    echo ""
    exit 1
fi

