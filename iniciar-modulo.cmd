@echo off
setlocal
if "%~1"=="" (
  echo Uso: iniciar-modulo.cmd nombre-de-la-carpeta
  pause
  exit /b 1
)
cd /d "%~dp0%~1"
call mvnw.cmd clean spring-boot:run
pause
