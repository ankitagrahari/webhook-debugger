FROM eclipse-temurin:21-jre-alpine
LABEL authors="ankitagrahari"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
