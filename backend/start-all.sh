#!/usr/bin/env bash
# Levanta RabbitMQ y los 6 microservicios (logs en ./logs). Detener: ./stop-all.sh
set -e
cd "$(dirname "$0")"
docker compose up -d
mkdir -p logs
sleep 12
(cd user-service && nohup mvn -q spring-boot:run > ../logs/user-service.log 2>&1 & echo $! > ../logs/user-service.pid)
(cd validation-service && nohup mvn -q spring-boot:run > ../logs/validation-service.log 2>&1 & echo $! > ../logs/validation-service.pid)
(cd question-service && nohup mvn -q spring-boot:run > ../logs/question-service.log 2>&1 & echo $! > ../logs/question-service.pid)
(cd lifecycle-service && nohup mvn -q spring-boot:run > ../logs/lifecycle-service.log 2>&1 & echo $! > ../logs/lifecycle-service.pid)
(cd review-service && nohup mvn -q spring-boot:run > ../logs/review-service.log 2>&1 & echo $! > ../logs/review-service.pid)
(cd notification-service && nohup mvn -q spring-boot:run > ../logs/notification-service.log 2>&1 & echo $! > ../logs/notification-service.pid)
echo "Servicios iniciando; revise ./logs"
