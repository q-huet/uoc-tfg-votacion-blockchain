#!/bin/bash
set -e

echo "Starting Spring Boot Backend..."

cd $(dirname "$0")/../backend-spring

# Ensure dependencies are installed (including the new grpc-netty-shaded)
./mvnw clean install -DskipTests

# Run the application
./mvnw spring-boot:run
