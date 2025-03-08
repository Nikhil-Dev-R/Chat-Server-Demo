# Use the official Gradle image to build the project
FROM gradle:8.4-jdk17 AS build

# Set the working directory
WORKDIR /app

# Copy Gradle files first (to optimize caching)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle gradle
RUN gradle build || true

# Copy the entire project
COPY . .

# Build the application (this generates the JAR)
RUN gradle build --no-daemon

# Use a minimal JDK image for running the app
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port (needed for WebSocket)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
