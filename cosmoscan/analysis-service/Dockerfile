FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y fontconfig
WORKDIR /app
COPY target/analysis-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]