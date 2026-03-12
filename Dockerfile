FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src

RUN mvn -q -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --from=build /workspace/target/steelworks-app-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
