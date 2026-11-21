@echo off
set "RAIZ=%~dp0"
echo Iniciando Eureka...
start "eureka-server" cmd /k call "%RAIZ%iniciar-modulo.cmd" eureka-server
timeout /t 25 /nobreak >nul
echo Iniciando perfiles...
start "perfiles-service" cmd /k call "%RAIZ%iniciar-modulo.cmd" perfiles-service
timeout /t 15 /nobreak >nul
echo Iniciando usuarios...
start "usuarios-service" cmd /k call "%RAIZ%iniciar-modulo.cmd" usuarios-service
timeout /t 15 /nobreak >nul
echo Iniciando sesiones...
start "sesiones-service" cmd /k call "%RAIZ%iniciar-modulo.cmd" sesiones-service
timeout /t 15 /nobreak >nul
echo Iniciando registros...
start "registros-service" cmd /k call "%RAIZ%iniciar-modulo.cmd" registros-service
timeout /t 15 /nobreak >nul
echo Iniciando gateway...
start "api-gateway" cmd /k call "%RAIZ%iniciar-modulo.cmd" api-gateway
echo.
echo Listo. Espera ~30 s y abre http://localhost:8761 (deben verse 5 servicios).
echo Para detener todo: cierra las 6 ventanas o presiona Ctrl+C en cada una.
pause
