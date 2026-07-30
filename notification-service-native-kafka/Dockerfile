FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY notification-service-spring-kafka/pom.xml notification-service-spring-kafka/pom.xml
COPY notification-service-native-kafka/pom.xml notification-service-native-kafka/pom.xml
RUN mvn -q -pl notification-service-native-kafka -am dependency:go-offline

COPY notification-service-native-kafka/src notification-service-native-kafka/src
RUN mvn -q -pl notification-service-native-kafka -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/notification-service-native-kafka/target/notification-service-native-kafka.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]