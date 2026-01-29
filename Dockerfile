FROM eclipse-temurin:21-jdk-jammy AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-jammy AS runtime
LABEL maintainer="test@test.com"

# Set working directory
WORKDIR /app

# Install curl for healthcheck and minimal cleanup
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring || true

# Copy the JAR file from the builder stage (use project artifact name)
COPY --from=builder /app/target/HelpDeskMCPServer-*.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown spring:spring logs

# Change ownership of the app directory
RUN chown -R spring:spring /app || true

# Switch to non-root user
USER spring

# Expose the port
EXPOSE 8082

# Health check (uses curl installed above)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

# Entry point
ENTRYPOINT ["java","-jar","/app/app.jar"]
