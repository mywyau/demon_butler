#!/bin/bash

# Stop and remove all services defined in the Compose files
echo "Stopping and removing existing containers..."
docker-compose -f docker-compose.traefik-base.yml -f docker-compose.traefik-frontend.yml -f docker-compose.traefik-backend.yml down --volumes

# Rebuild and start all services cleanly
echo "Rebuilding and starting services..."
docker-compose -f docker-compose.traefik-base.yml -f docker-compose.traefik-frontend.yml -f docker-compose.traefik-backend.yml up --build -d

# Confirm services are running
echo "All services are up and running!"
docker ps
