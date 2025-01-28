#!/bin/bash

# Stop and remove all services defined in the Compose files (including images)
echo "Stopping and removing existing containers and images..."
docker-compose -f docker-compose.wander-frontend.yml -f docker-compose.reggie-frontend.yml -f docker-compose.cashew-backend.yml -f docker-compose.pistachio-backend.yml -f docker-compose.traefik-only.yml down --volumes --rmi all