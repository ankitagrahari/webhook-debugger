#!/bin/bash
# HookSpy deployment script
# Usage: ./deploy.sh

set -e

echo "Building all services..."
mvn clean package -DskipTests -Pproduction

echo "Building Docker images..."
docker build -t hookspy/capture-service:latest capture-service/
docker build -t hookspy/processor-service:latest processor-service/
docker build -t hookspy/ui-service:latest ui-service/

echo "Starting stack..."
docker compose -f docker-compose.prod.yml --env-file .env up -d

echo "Waiting for services..."
sleep 10

echo "Health check..."
curl -sf http://localhost:8080/actuator/health && echo "capture-service OK"
curl -sf http://localhost:8081/actuator/health && echo "processor-service OK"
curl -sf http://localhost:8082/actuator/health && echo "ui-service OK"

echo "Deploy complete."