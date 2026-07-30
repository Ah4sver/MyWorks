FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY notification-service-spring-kafka/pom.xml notification-service-spring-kafka/pom.xml
COPY notification-service-native-kafka/pom.xml notification-service-native-kafka/pom.xml
RUN mvn -q -pl notification-service-spring-kafka -am dependency:go-offline

COPY notification-service-spring-kafka/src notification-service-spring-kafka/src
RUN mvn -q -pl notification-service-spring-kafka -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/notification-service-spring-kafka/target/notification-service-spring-kafka.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]