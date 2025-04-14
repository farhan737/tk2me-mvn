#!/bin/bash

# Start your Spring Boot application in the background
echo "Starting your Spring Boot application on port 8080..."
./mvnw spring-boot:run &
APP_PID=$!

# Give the application some time to start
echo "Waiting for application to start..."
sleep 10

# Start ngrok with your custom domain
echo "Starting ngrok tunnel to model-bunny-just.ngrok-free.app..."
/snap/bin/ngrok http --domain=model-bunny-just.ngrok-free.app 8080

# When ngrok is terminated, also terminate the application
kill $APP_PID
