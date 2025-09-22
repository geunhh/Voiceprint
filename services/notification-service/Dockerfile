# Build Stage
FROM gradle:8.14.0-jdk21-alpine AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN ./gradlew :services:notification-service:build -x test --no-daemon

# Final App Stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /home/gradle/src/services/notification-service/build/libs/notification-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]