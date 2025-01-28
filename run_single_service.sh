#!/bin/bash

# Check if a compose file is passed as an argument
if [ -z "$1" ]; then
  echo "Usage: $0 <docker-compose-file>"
  echo "Example: $0 docker-compose.wander-frontend.yml"
  exit 1
fi

COMPOSE_FILE=$1

# Rebuild and start the services defined in the specified Compose file
echo "Rebuilding and starting services defined in $COMPOSE_FILE with no cache..."

# Rebuild the services with no cache
docker-compose -f "$COMPOSE_FILE" build --no-cache

# Start the services in detached mode
docker-compose -f "$COMPOSE_FILE" up -d

# Confirm services are running
echo "Services from $COMPOSE_FILE are up and running!"
docker ps
