#!/bin/bash

# Check if a compose file is passed as an argument
if [ -z "$1" ]; then
  echo "Usage: $0 <docker-compose-file>"
  echo "Example: $0 docker-compose.wander-frontend.yml"
  exit 1
fi

COMPOSE_FILE=$1

# Stop and remove all services defined in the specified Compose file
echo "Stopping and removing services defined in $COMPOSE_FILE..."
docker-compose -f "$COMPOSE_FILE" down --volumes --rmi all
