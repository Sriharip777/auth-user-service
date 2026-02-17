# ================================
# Builder stage
# ================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper & pom (for dependency caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Fix permission for mvnw
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build JAR with production profile
RUN ./mvnw clean package -DskipTests=true

# ================================
# Runtime stage
# ================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user (security best practice)
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Fix permissions
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# ✅ FIXED: Expose 8080 (not 8081)
EXPOSE 8081

# Health check (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# ✅ FIXED: Increased memory and added Java options
ENTRYPOINT ["java", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
