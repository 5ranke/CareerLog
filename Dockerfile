FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src
COPY frontend ./frontend
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
RUN useradd --system --uid 1001 careerlog
COPY --from=builder /workspace/build/libs/*.jar /app/careerlog.jar
USER careerlog

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/careerlog.jar"]
