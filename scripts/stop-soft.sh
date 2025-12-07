#!/bin/bash
set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}=== Pausing Voting Blockchain System ===${NC}"

# 1. Stop Backend and Frontend
echo -e "${GREEN}[1/2] Stopping Applications...${NC}"
# Kill processes listening on ports 8080 (Spring) and 4200 (Angular)
# Using fuser if available, otherwise pkill
if command -v fuser &> /dev/null; then
    fuser -k 8080/tcp 2>/dev/null || true
    fuser -k 4200/tcp 2>/dev/null || true
else
    pkill -f "java.*votacion" || true
    pkill -f "ng serve" || true
fi

# 2. Stop Docker Containers (without removing them)
echo -e "${GREEN}[2/2] Pausing Fabric Network Containers...${NC}"
# Filter containers belonging to the fabric network
CONTAINERS=$(docker ps -q --filter network=fabric_test)

if [ -n "$CONTAINERS" ]; then
    docker stop $CONTAINERS
    echo "Containers stopped successfully."
else
    echo "No running Fabric containers found."
fi

echo -e "${GREEN}✅ System paused successfully.${NC}"
echo "Data is preserved in Docker containers."
echo "To resume, run: ./scripts/resume-soft.sh"
