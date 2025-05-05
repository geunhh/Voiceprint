# 빌드 스테이지 
FROM gradle:8.14.0-jdk21-alpine AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . .

RUN chmod +x gradlew

RUN ./gradlew clean build -x test --no-daemon

# Spring Boot 앱 실행
FROM openjdk:21-jdk

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 18080 19090

# 실행 명령
ENTRYPOINT ["java", "-jar", "/app/app.jar"]