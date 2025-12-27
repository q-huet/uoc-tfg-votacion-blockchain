#!/bin/bash
set -e

# Add Fabric binaries to PATH
export PATH=${PWD}/../fabric-samples/bin:$PATH
export FABRIC_CFG_PATH=${PWD}/../fabric-samples/config/

echo "Starting Hyperledger Fabric Network..."
echo "Fabric Binaries Path: $(which peer)"

# Navigate to test-network
cd $(dirname "$0")/../fabric-samples/test-network


# Tear down any existing network
echo "Cleaning up previous network..."
./network.sh down

# Force cleanup of potential zombie containers/volumes
docker rm -f $(docker ps -aq --filter label=service=hyperledger-fabric) 2>/dev/null || true
docker volume prune -f 2>/dev/null || true

# Start network and create channel with CouchDB
echo "Creating network and channel 'electionchannel' with CouchDB..."
./network.sh up createChannel -c electionchannel -s couchdb

# Check if peers are running
if ! docker ps | grep -q "peer0.org1.example.com"; then
    echo "ERROR: Peer0 Org1 container is NOT running!"
    docker ps -a
    docker logs peer0.org1.example.com
    exit 1
fi

# Deploy chaincode

# Deploy chaincode
echo "Deploying chaincode 'electioncc'..."
# Increase client timeout to avoid "context deadline exceeded" during commit
export CORE_PEER_CLIENT_REQUESTTIMEOUT=300s
export FABRIC_CLIENT_TIMEOUT=300s

# We use the java chaincode located in chaincode/java
# Note: The chaincode project must have 'rootProject.name = "electioncc"' in settings.gradle
./network.sh deployCC -ccn electioncc -ccp ../../chaincode/java -ccl java -c electionchannel

echo "Network started and chaincode deployed successfully!"
