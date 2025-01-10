#!/bin/bash

# Stop and remove all services defined in the Compose files (including images)
echo "Stopping and removing existing containers and images..."
docker-compose -f docker-compose.traefik-base.yml -f docker-compose.traefik-frontend.yml -f docker-compose.traefik-backend.yml down --volumes --rmi all

# Rebuild and start all services cleanly, ignoring cache
echo "Rebuilding and starting services from scratch..."
docker-compose -f docker-compose.traefik-base.yml -f docker-compose.traefik-frontend.yml -f docker-compose.traefik-backend.yml up --build -d

# Confirm services are running
echo "All services are up and running!"
docker ps
