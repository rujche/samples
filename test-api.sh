#!/bin/bash

echo "Starting Micronaut application..."
cd "c:/Users/rujche/Work/git-repositories/feature-1/rujche_microsoft_example"
java -jar build/libs/micronaut-redis-app-0.1-all.jar &
APP_PID=$!

echo "Waiting for application to start..."
sleep 5

echo ""
echo "=== Testing API Endpoints ==="
echo ""

echo "1. Testing health endpoint..."
curl -s http://localhost:8080/api/items/health
echo ""
echo ""

echo "2. Creating an item..."
curl -s -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"id": "1", "name": "Sample Item", "description": "This is a sample item"}'
echo ""
echo ""

echo "3. Getting the item..."
curl -s http://localhost:8080/api/items/1
echo ""
echo ""

echo "4. Updating the item..."
curl -s -X PUT http://localhost:8080/api/items/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated Item", "description": "This is an updated item"}'
echo ""
echo ""

echo "5. Getting the updated item..."
curl -s http://localhost:8080/api/items/1
echo ""
echo ""

echo "6. Deleting the item..."
curl -s -X DELETE http://localhost:8080/api/items/1
echo ""
echo ""

echo "7. Trying to get deleted item (should return 404)..."
curl -s -w "\nHTTP Status: %{http_code}\n" http://localhost:8080/api/items/1
echo ""

echo ""
echo "=== Stopping application ==="
kill $APP_PID
echo "Application stopped."
