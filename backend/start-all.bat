@echo off
REM Levanta RabbitMQ y abre una ventana por cada microservicio.
docker compose up -d
echo Esperando a RabbitMQ...
timeout /t 15 /nobreak > nul
start "user-service" cmd /k "cd /d %~dp0user-service && mvn spring-boot:run"
start "validation-service" cmd /k "cd /d %~dp0validation-service && mvn spring-boot:run"
start "question-service" cmd /k "cd /d %~dp0question-service && mvn spring-boot:run"
start "lifecycle-service" cmd /k "cd /d %~dp0lifecycle-service && mvn spring-boot:run"
start "review-service" cmd /k "cd /d %~dp0review-service && mvn spring-boot:run"
start "notification-service" cmd /k "cd /d %~dp0notification-service && mvn spring-boot:run"
