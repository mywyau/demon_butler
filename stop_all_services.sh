#!/bin/bash

# Stop and remove all services defined in the Compose files (including images)
echo "Stopping and removing existing containers and images..."
docker-compose -f docker-compose.traefik-base.yml -f docker-compose.traefik-frontend.yml -f docker-compose.traefik-backend.yml down --volumes --rmi all