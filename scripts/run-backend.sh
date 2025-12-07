#!/bin/bash
set -e

echo "Starting Spring Boot Backend..."

cd $(dirname "$0")/../backend-spring

# Run the application (Spring Boot will handle compilation)
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8080"
