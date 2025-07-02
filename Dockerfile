FROM eclipse-temurin:23-jdk AS builder

WORKDIR /app
COPY . .
COPY .env .env
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:23-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/.env .env

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
