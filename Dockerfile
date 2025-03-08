# Use a lightweight JDK image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file (if using Gradle, use `build/libs/*.jar`)
COPY build/libs/*.jar app.jar

# Expose the port (needed for WebSocket connections)
EXPOSE 8080

# Start the application
CMD ["java", "-jar", "app.jar"]
