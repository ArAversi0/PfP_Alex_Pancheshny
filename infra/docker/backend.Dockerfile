FROM eclipse-temurin:17-jre

WORKDIR /opt/pfp
COPY apps/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

