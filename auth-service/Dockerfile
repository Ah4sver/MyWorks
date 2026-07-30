FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY notification-service-spring-kafka/pom.xml notification-service-spring-kafka/pom.xml
COPY notification-service-native-kafka/pom.xml notification-service-native-kafka/pom.xml
RUN mvn -q -pl auth-service -am dependency:go-offline

COPY auth-service/src auth-service/src
RUN mvn -q -pl auth-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/auth-service/target/auth-service.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]