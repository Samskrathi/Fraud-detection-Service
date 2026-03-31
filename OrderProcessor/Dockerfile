FROM gradle:8.14-jdk17 AS builder
WORKDIR /workspace

COPY settings.gradle build.gradle.kts gradlew gradlew.bat ./
COPY gradle gradle
COPY OrderProcessor OrderProcessor

RUN chmod +x gradlew && ./gradlew :OrderProcessor:bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /workspace/OrderProcessor/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
