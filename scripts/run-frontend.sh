#!/bin/bash
set -e

echo "Starting Angular Frontend..."

cd $(dirname "$0")/../frontend-angular

# Check if node_modules exists, if not install dependencies
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

# Run the application
echo "Serving application on http://localhost:4200"
npm start
