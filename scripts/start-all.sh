#!/bin/bash
set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Starting Voting Blockchain System ===${NC}"

# 0. Kill existing processes (Backend/Frontend)
echo -e "${BLUE}Stopping existing processes...${NC}"
if command -v fuser &> /dev/null; then
    fuser -k 8080/tcp 2>/dev/null || true
    fuser -k 4200/tcp 2>/dev/null || true
else
    pkill -f "java.*votacion" || true
    pkill -f "ng serve" || true
fi

# 0.1 Clean up local data (Hard Reset)
echo -e "${BLUE}Cleaning up local backend data...${NC}"
rm -rf backend-spring/data/elections-db.json
rm -rf backend-spring/data/storage/*
rm -rf backend-spring/wallet/*

# Stop and clean Explorer
echo "Cleaning up Explorer..."
if [ -d "explorer" ]; then
    pushd explorer > /dev/null
    docker-compose down -v 2>/dev/null || true
    popd > /dev/null
fi

echo "Local data cleaned."

# 1. Start Network
echo -e "${GREEN}[1/3] Starting Hyperledger Fabric Network...${NC}"
./scripts/start-network.sh

# 1.5 Start Hyperledger Explorer
echo -e "${GREEN}[1.5/3] Starting Hyperledger Explorer...${NC}"
pushd explorer > /dev/null
docker-compose up -d
popd > /dev/null
echo "Explorer started at http://localhost:8090"

# 2. Start Backend
echo -e "${GREEN}[2/3] Starting Spring Boot Backend...${NC}"
# Run in background and save PID
./scripts/run-backend.sh > backend-spring/logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend starting with PID $BACKEND_PID. Logs are being written to backend-spring/logs/backend.log"

# Wait for Backend to be ready (check for port 8080)
echo "Waiting for Backend to initialize..."
MAX_RETRIES=150 # 5 minutes
COUNT=0
while ! nc -z localhost 8080; do
    sleep 2
    COUNT=$((COUNT+1))
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo "Error: Backend failed to start in time."
        echo "=== Backend Logs (Last 50 lines) ==="
        tail -n 50 backend-spring/logs/backend.log
        echo "===================================="
        kill $BACKEND_PID
        exit 1
    fi
    echo -n "."
done
echo -e "\nBackend is UP!"

# 3. Start Frontend
echo -e "${GREEN}[3/3] Starting Angular Frontend...${NC}"
./scripts/run-frontend.sh
