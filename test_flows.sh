#!/bin/bash

# Base URL
URL="http://localhost:8080"

# 1. Login
echo "--- Logging in ---"
RESPONSE=$(curl -s -X POST $URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@acme.com", "password": "password"}')

TOKEN=$(echo $RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Received Token: $TOKEN"

# 2. Get Current User (/auth/me)
echo -e "\n--- Current User ---"
curl -s -X GET $URL/auth/me \
  -H "Authorization: Bearer $TOKEN"

# 3. Create Project
echo -e "\n--- Create Project ---"
PROJECT_ID=$(curl -s -X POST $URL/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Project",
    "description": "Project Description"
  }' | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -n 1)
echo "Created Project ID: $PROJECT_ID"

# 4. Create Task
echo -e "\n--- Add Task to Project ---"
curl -s -X POST $URL/projects/$PROJECT_ID/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "First Task",
    "description": "Task desc",
    "status": "TODO"
  }'

# 5. List Projects
echo -e "\n--- List Projects ---"
curl -s -X GET $URL/projects \
  -H "Authorization: Bearer $TOKEN"

# 6. Transaction Rollback Demo
echo -e "\n--- Transaction Rollback Demo (Fails) ---"
curl -s -X POST $URL/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Failing Project",
    "tasks": [
      { "title": "FAIL_TRANSACTION", "description": "Trigger Rollback" }
    ]
  }'
