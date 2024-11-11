# Build stage
FROM amazoncorretto:23 AS build
WORKDIR /app

# Install required packages
RUN yum update -y && \
    yum install -y wget unzip findutils which tar gzip

# Install Gradle
RUN wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip && \
    mv gradle-8.5 /opt/gradle && \
    rm gradle-8.5-bin.zip

# Set Gradle in PATH
ENV PATH="${PATH}:/opt/gradle/bin"

# Copy only the build files first for better caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY gradlew gradlew.bat ./

# Give execute permission to gradlew
RUN chmod +x gradlew

# Copy the rest of the application
COPY . .

# Build with gradlew instead of gradle
RUN ./gradlew build -x test --no-daemon

# Run stage
FROM amazoncorretto:23-alpine-jdk
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy the jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Add healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

EXPOSE 8080

# Add memory and garbage collection options
ENV JAVA_OPTS="-XX:+EnableDynamicAgentLoading -Xmx512m -XX:+UseG1GC"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]