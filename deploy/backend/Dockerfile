FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S -g 10001 pe && adduser -S -u 10001 -G pe pe
WORKDIR /app
COPY --chown=pe:pe backend/target/personal-economy-backend-0.1.0-SNAPSHOT.jar /app/app.jar
USER pe
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
