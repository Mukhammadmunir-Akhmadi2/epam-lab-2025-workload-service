FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
ENV SPRING_DATA_MONGODB_URI=mongodb://host.docker.internal
RUN mvn clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /app/data
ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]