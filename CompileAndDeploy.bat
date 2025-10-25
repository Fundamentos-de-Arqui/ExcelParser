@echo off
echo ==========================================
echo === COMPILACION Y DESPLIEGUE COMPLETO ===
echo ==========================================
echo.

echo 1. Limpiando proyecto...
call mvn clean
if %errorlevel% neq 0 (
    echo ❌ Error en mvn clean
    exit /b 1
)
echo ✅ Limpieza completada
echo.

echo 2. Compilando proyecto...
call mvn compile
if %errorlevel% neq 0 (
    echo ❌ Error en mvn compile
    exit /b 1
)
echo ✅ Compilación completada
echo.

echo 3. Empaquetando proyecto...
call mvn package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ Error en mvn package
    exit /b 1
)
echo ✅ Empaquetado completado
echo.

echo 4. Verificando que se genero el WAR...
if exist "target\DocExcelParser.war" (
    echo ✅ DocExcelParser.war generado correctamente
    echo    Ubicación: target\DocExcelParser.war
    echo    Tamaño: 
    dir "target\DocExcelParser.war" | findstr "DocExcelParser.war"
) else (
    echo ❌ Error: DocExcelParser.war no se genero
    exit /b 1
)

echo.
echo ==========================================
echo === COMPILACION COMPLETADA ===
echo ==========================================
echo.
echo 📋 Próximos pasos para despliegue:
echo    1. Elimina el WAR actual de WildFly deployments
echo    2. Copia target\DocExcelParser.war a:
echo       C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\standalone\deployments\
echo    3. WildFly detectará automáticamente el cambio y redesplegará
echo    4. Verifica en los logs de WildFly que se redesplegó correctamente
echo    5. Prueba el hello servlet: http://localhost:8080/DocExcelParser/hello-servlet
echo    6. Ejecuta el script de prueba del reverse parser
echo.
echo 📋 Archivo listo para copiar:
echo    target\DocExcelParser.war
echo.

pause
