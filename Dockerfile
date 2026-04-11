# A Dockerfile for a Java/Spring Boot application built with Gradle

# Use the official Gradle image to build the application
FROM gradle:7.2-jdk11 AS builder
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Use the official OpenJDK image to run the application
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
