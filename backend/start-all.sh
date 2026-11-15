#!/usr/bin/env bash
# Construye e inicia Keycloak, RabbitMQ y los seis microservicios.
set -e
cd "$(dirname "$0")"
docker compose up --build -d
docker compose ps
