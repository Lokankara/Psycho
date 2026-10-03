FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar
RUN mkdir -p /app/data

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
