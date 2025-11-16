@echo off
echo ==========================================
echo === DIAGNOSTICO DE LECTURA DE COLA ===
echo === REPLICA LA LOGICA DEL PROYECTO ===
echo ==========================================
echo.

echo [1/3] Compilando proyecto...
call mvn compile -q
if %errorlevel% neq 0 (
    echo.
    echo ERROR: No se pudo compilar el proyecto
    echo Verifica que Maven este instalado y configurado correctamente
    pause
    exit /b 1
)
echo OK - Proyecto compilado
echo.

echo [2/3] Compilando DiagnoseQueueReader...
javac -cp "target/classes;target/DocExcelParser/WEB-INF/lib/*" DiagnoseQueueReader.java
if %errorlevel% neq 0 (
    echo.
    echo ERROR: No se pudo compilar DiagnoseQueueReader
    echo Verifica que todas las dependencias esten disponibles
    pause
    exit /b 1
)
echo OK - DiagnoseQueueReader compilado
echo.

echo [3/3] Ejecutando diagnostico...
echo.
java -cp ".;target/classes;target/DocExcelParser/WEB-INF/lib/*" DiagnoseQueueReader

echo.
pause

