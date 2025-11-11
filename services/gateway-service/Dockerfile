FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar", "--spring.cloud.config.uri=http://config-service:8888"]
EXPOSE 8000