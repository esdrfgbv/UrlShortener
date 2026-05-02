# Build stage
FROM maven:3.8.4-eclipse-temurin-8 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:8-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port (Render uses 8080 by default)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
