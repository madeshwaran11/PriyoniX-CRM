# Runtime image for PriyoniX CRM
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR built locally with Maven
COPY target/crm-0.0.1-SNAPSHOT.jar app.jar

# Render supplies the PORT environment variable
EXPOSE 8080

# Start Spring Boot with the Supabase profile
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-supabase} -jar /app/app.jar"]
