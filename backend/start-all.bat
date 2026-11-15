@echo off
REM Construye e inicia Keycloak, RabbitMQ y los seis microservicios.
cd /d "%~dp0"
docker compose up --build -d
if errorlevel 1 exit /b %errorlevel%
echo Stack iniciado. Verifica los servicios con: docker compose ps
echo Keycloak: http://localhost:8180  |  APIs: puertos 8081-8086
