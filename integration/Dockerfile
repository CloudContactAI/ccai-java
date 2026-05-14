FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /sdk
COPY pom.xml ./
RUN mvn dependency:go-offline -q || true

COPY src/ ./src/
RUN mvn install -DskipTests -Dgpg.skip=true -q

WORKDIR /sdk/integration
COPY integration/pom.xml ./
RUN mvn dependency:go-offline -q || true

COPY integration/ ./
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /sdk/integration/target/ccai-integration-test-java-1.0-shaded.jar ./test.jar
CMD ["java", "-jar", "test.jar"]
