@echo off
echo ==========================================
echo === VERIFICACION DE CONEXION ACTIVEMQ ===
echo ==========================================
echo.

set BROKER_IP=172.193.242.89
set BROKER_PORT=61616
set HTTP_PORT=8161

echo [1/4] Verificando conectividad de red...
echo Ping a %BROKER_IP%...
ping -n 4 %BROKER_IP% >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Servidor accesible
) else (
    echo ❌ No se puede alcanzar el servidor
    echo    Verifica tu conexión de red o VPN
    pause
    exit /b 1
)

echo.
echo [2/4] Verificando puerto JMS (61616)...
powershell -Command "$result = Test-NetConnection -ComputerName %BROKER_IP% -Port %BROKER_PORT% -InformationLevel Quiet -WarningAction SilentlyContinue; if ($result) { Write-Host '✅ Puerto 61616 accesible' } else { Write-Host '❌ Puerto 61616 NO accesible - Verifica firewall' }"

echo.
echo [3/4] Verificando consola web HTTP (8161)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://%BROKER_IP%:%HTTP_PORT%' -TimeoutSec 5 -UseBasicParsing; Write-Host '✅ Consola web accesible (HTTP %HTTP_PORT%)' } catch { Write-Host '❌ Consola web NO accesible - Verifica firewall o que ActiveMQ esté corriendo' }"

echo.
echo [4/4] Verificando API REST de ActiveMQ...
powershell -Command "$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('admin:admin')); try { $response = Invoke-WebRequest -Uri 'http://%BROKER_IP%:%HTTP_PORT%/api/jolokia/version' -Headers @{Authorization=('Basic ' + $cred)} -TimeoutSec 5 -UseBasicParsing; Write-Host '✅ API REST accesible' } catch { Write-Host '⚠️  API REST no accesible o requiere autenticación diferente' }"

echo.
echo ==========================================
echo === VERIFICACION COMPLETA ===
echo ==========================================
echo.
echo Si todas las verificaciones pasaron, la conexión debería funcionar.
echo Si hay problemas, verifica:
echo   1. Firewall de Windows
echo   2. Firewall de Azure (Network Security Groups)
echo   3. Que ActiveMQ esté corriendo en el servidor
echo   4. Que los puertos estén abiertos en Azure
echo.
pause

