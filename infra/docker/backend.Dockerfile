FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew && ./gradlew :apps:backend:bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /opt/pfp
COPY --from=build /workspace/apps/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
