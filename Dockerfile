FROM eclipse-temurin:25-jdk
WORKDIR /workspace

COPY . .
RUN chmod +x gradlew

EXPOSE 8080 8081 5173 5174

CMD ["./gradlew", ":rest:bootRun", ":ui:bootRun", ":kanban:bootRun", ":agile:bootRun", "--parallel"]
