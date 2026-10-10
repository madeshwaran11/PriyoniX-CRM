# ==========================================
# PRIYONIX CRM - SPRING BOOT BACKEND
# Java 21 + Maven
# ==========================================

# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration and source
COPY pom.xml .
COPY src ./src

# Build the Spring Boot executable JAR
RUN mvn -B -DskipTests clean package


# ==========================================
# Runtime stage
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated application JAR
COPY --from=build /app/target/*.jar /app/app.jar

# Application port
EXPOSE 8080

# Start using the Supabase profile by default
# Render's PORT environment variable is respected
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-supabase} -jar /app/app.jar"]
