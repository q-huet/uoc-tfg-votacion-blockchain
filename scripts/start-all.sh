#!/bin/bash
set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Starting Voting Blockchain System ===${NC}"

# 1. Start Network
echo -e "${GREEN}[1/3] Starting Hyperledger Fabric Network...${NC}"
./scripts/start-network.sh

# 2. Start Backend
echo -e "${GREEN}[2/3] Starting Spring Boot Backend...${NC}"
# Run in background and save PID
./scripts/run-backend.sh > backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend starting with PID $BACKEND_PID. Logs are being written to backend.log"

# Wait for Backend to be ready (check for port 8080)
echo "Waiting for Backend to initialize..."
MAX_RETRIES=30
COUNT=0
while ! nc -z localhost 8080; do
    sleep 2
    COUNT=$((COUNT+1))
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo "Error: Backend failed to start in time."
        cat backend.log
        kill $BACKEND_PID
        exit 1
    fi
    echo -n "."
done
echo -e "\nBackend is UP!"

# 3. Start Frontend
echo -e "${GREEN}[3/3] Starting Angular Frontend...${NC}"
./scripts/run-frontend.sh
