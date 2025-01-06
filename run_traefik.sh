#!/bin/bash

# Stop the containers
docker-compose -f docker-compose.traefik.yml down

# Rebuild the images
docker-compose -f docker-compose.traefik.yml build

# Start the containers
docker-compose -f docker-compose.traefik.yml up -d

# Confirm the status of containers
docker-compose -f docker-compose.traefik.yml ps
