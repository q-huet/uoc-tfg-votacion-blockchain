#!/bin/bash
set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}=== Resuming Voting Blockchain System ===${NC}"

# 1. Start Docker Containers
echo -e "${GREEN}[1/3] Resuming Fabric Network...${NC}"
# Find all containers (running or stopped) related to the network
CONTAINERS=$(docker ps -a -q --filter network=fabric_test)

if [ -n "$CONTAINERS" ]; then
    docker start $CONTAINERS
    echo "Containers started."
    
    # Wait for containers to be fully ready
    echo "Waiting 10s for peers to stabilize..."
    sleep 10
else
    echo "No Fabric containers found. Did you run start-all.sh first?"
    exit 1
fi

# 2. Start Backend
echo -e "${GREEN}[2/3] Starting Spring Boot Backend...${NC}"
./scripts/run-backend.sh > backend-spring/logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend starting with PID $BACKEND_PID"

# Wait for Backend
echo "Waiting for Backend to initialize..."
MAX_RETRIES=150
COUNT=0
while ! nc -z localhost 8080; do
    sleep 2
    COUNT=$((COUNT+1))
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo "Error: Backend failed to start."
        tail -n 50 backend-spring/logs/backend.log
        kill $BACKEND_PID
        exit 1
    fi
    echo -n "."
done
echo -e "\nBackend is UP!"

# 3. Start Frontend
echo -e "${GREEN}[3/3] Starting Angular Frontend...${NC}"
./scripts/run-frontend.sh
