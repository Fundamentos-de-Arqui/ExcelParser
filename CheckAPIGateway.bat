@echo off
echo === VERIFICANDO API GATEWAY ===
echo.

echo Probando conexion al API Gateway en localhost:4000...
curl -s -o nul -w "Status: %%{http_code}\n" http://localhost:4000/api/excel/generated-link

if %errorlevel% equ 0 (
    echo.
    echo API Gateway parece estar ejecutandose
    echo.
    echo Ahora probando el endpoint especifico...
    curl -X POST ^
      -H "Content-Type: application/json" ^
      -d "{\"test\":\"connection\"}" ^
      http://localhost:4000/api/excel/generated-link
) else (
    echo.
    echo API Gateway no esta ejecutandose o no responde
    echo.
    echo Para iniciar el API Gateway:
    echo 1. Ve a la carpeta del API Gateway
    echo 2. Ejecuta: npm start
    echo 3. Deberia iniciarse en el puerto 4000
)

echo.
echo === VERIFICACION COMPLETADA ===
pause
