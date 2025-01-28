#!/bin/bash

# Rebuild and start all services cleanly, forcing no cache during the build
echo "Rebuilding and starting services from scratch with no cache..."

docker-compose -f docker-compose.wander-frontend.yml -f docker-compose.reggie-frontend.yml -f docker-compose.cashew-backend.yml -f docker-compose.pistachio-backend.yml -f docker-compose.traefik-only.yml build --no-cache

docker-compose -f docker-compose.wander-frontend.yml -f docker-compose.reggie-frontend.yml -f docker-compose.cashew-backend.yml -f docker-compose.pistachio-backend.yml -f docker-compose.traefik-only.yml up -d

# Confirm services are running
echo "All services are up and running!"
docker ps
