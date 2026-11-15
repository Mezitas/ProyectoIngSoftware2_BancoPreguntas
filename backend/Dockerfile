FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
ARG SERVICE
RUN mvn -B -pl "${SERVICE}" -am package -DskipTests

FROM eclipse-temurin:17-jre
ARG SERVICE
RUN useradd --system --uid 10001 --create-home app \
    && mkdir -p /data \
    && chown -R app:app /data
WORKDIR /app
COPY --from=build "/workspace/${SERVICE}/target/${SERVICE}-1.0.0.jar" app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
