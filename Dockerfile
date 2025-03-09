# Use the official Gradle image to build the project
FROM gradle:8.4-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper & config files first (to optimize caching)
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle gradle

# Ensure Gradle wrapper has execution permissions
RUN chmod +x gradlew

# Download dependencies separately to improve caching
RUN ./gradlew dependencies --no-daemon

# Copy the entire project
COPY . .

# Build the application
RUN ./gradlew build --no-daemon

# Use a stable JDK image to run the application
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port (needed for WebSocket)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
