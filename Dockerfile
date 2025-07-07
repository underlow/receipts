
# Build stage
FROM openjdk:17-jdk-slim AS builder

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Production stage
FROM openjdk:17-jdk-slim

# Create a non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set work directory
WORKDIR /app

# Copy the jar file
COPY build/libs/*.jar app.jar

# Create directories for data with proper permissions
RUN mkdir -p /data/inbox /data/attachments && \
    chown -R appuser:appuser /data && \
    chown -R appuser:appuser /app

# Set up environment variables with defaults
ENV SPRING_PROFILES_ACTIVE=prod
ENV ALLOWED_EMAILS=""
ENV SESSION_TIMEOUT=3600
ENV LOG_LEVEL=INFO
ENV INBOX_PATH=/data/inbox
ENV ATTACHMENTS_PATH=/data/attachments

# Expose port
EXPOSE 8080

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
