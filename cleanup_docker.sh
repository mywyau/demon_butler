#!/bin/bash

echo "Starting Docker cleanup..."

# Remove dangling images
echo "Removing dangling images..."
removed_images=$(docker image prune -f --quiet | wc -l)
echo "Removed $removed_images dangling images."

# Remove dangling volumes
echo "Removing dangling volumes..."
removed_volumes=$(docker volume prune -f --quiet | wc -l)
echo "Removed $removed_volumes dangling volumes."

# Remove unused build cache
echo "Removing unused build cache..."
removed_cache=$(docker builder prune --all -f --quiet | wc -l)
echo "Removed $removed_cache build cache layers."

# Final system-wide cleanup
echo "Performing a final system prune (containers, networks, images)..."
docker system prune -f

echo "Docker cleanup complete!"
