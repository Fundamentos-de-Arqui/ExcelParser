@echo off
echo ==========================================
echo === RECONSTRUCCION COMPLETA DE LA APP ===
echo ==========================================
echo.

echo 1. Limpiando proyecto...
call mvn clean

echo.
echo 2. Compilando proyecto...
call mvn compile

echo.
echo 3. Empaquetando proyecto...
call mvn package -DskipTests

echo.
echo 4. Verificando que se genero el WAR...
if exist "target\DocExcelParser.war" (
    echo ✅ DocExcelParser.war generado correctamente
    echo    Tamaño: 
    dir "target\DocExcelParser.war" | findstr "DocExcelParser.war"
) else (
    echo ❌ Error: DocExcelParser.war no se genero
    exit /b 1
)

echo.
echo 5. Copiando WAR a WildFly deployments...
copy "target\DocExcelParser.war" "C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\standalone\deployments\"

if %errorlevel% equ 0 (
    echo ✅ WAR copiado exitosamente a WildFly deployments
) else (
    echo ❌ Error copiando WAR a WildFly deployments
    exit /b 1
)

echo.
echo 6. Verificando despliegue...
if exist "C:\Users\suiny\Desktop\wildfly\wildfly-37.0.1.Final\standalone\deployments\DocExcelParser.war" (
    echo ✅ WAR desplegado correctamente en WildFly
) else (
    echo ❌ Error: WAR no encontrado en deployments
    exit /b 1
)

echo.
echo ==========================================
echo === RECONSTRUCCION COMPLETADA ===
echo ==========================================
echo.
echo 📋 Próximos pasos:
echo    1. Reinicia WildFly desde el IDE
echo    2. Verifica que el servidor esté funcionando (indicador verde)
echo    3. Prueba el hello servlet: http://localhost:8080/DocExcelParser/hello-servlet
echo    4. Si funciona, ejecuta el script de prueba del reverse parser
echo.

pause
