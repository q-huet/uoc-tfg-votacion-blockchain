#!/bin/bash
set -e

echo "Starting Hyperledger Fabric Network..."

# Navigate to test-network
cd $(dirname "$0")/../fabric/test-network

# Tear down any existing network
echo "Cleaning up previous network..."
./network.sh down

# Start network and create channel with CouchDB
echo "Creating network and channel 'electionchannel' with CouchDB..."
./network.sh up createChannel -c electionchannel -s couchdb

# Deploy chaincode
echo "Deploying chaincode 'electioncc'..."
# We use the java chaincode located in chaincode/java
# Note: The chaincode project must have 'rootProject.name = "electioncc"' in settings.gradle
./network.sh deployCC -ccn electioncc -ccp ../../chaincode/java -ccl java -c electionchannel

echo "Network started and chaincode deployed successfully!"
