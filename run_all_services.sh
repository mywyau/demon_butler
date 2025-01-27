#!/bin/bash

# Rebuild and start all services cleanly, forcing no cache during the build
echo "Rebuilding and starting services from scratch with no cache..."
docker-compose -f docker-compose.traefik-base.yml -f docker-compose.traefik-frontend.yml -f docker-compose.traefik-backend.yml build --no-cache
docker-compose -f docker-compose.traefik-base.yml -f docker-compose.traefik-frontend.yml -f docker-compose.traefik-backend.yml up -d

# Confirm services are running
echo "All services are up and running!"
docker ps
