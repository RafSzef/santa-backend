# Stage 1: Build Spring Boot application with Maven & Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Cache dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Build executable jar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Minimal runtime image with JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user
RUN addgroup -S santa && adduser -S santa -G santa
USER santa:santa

COPY --from=build /app/target/santa-backend-*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
