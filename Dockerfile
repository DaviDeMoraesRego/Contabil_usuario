FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve dependency:resolve-plugins -q || true
COPY src src
RUN ./mvnw -B package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S -g 1001 appgroup && adduser -S -u 1001 -G appgroup appuser

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /build/target/contabil-usuario-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/log && chown -R appuser:appgroup /app
VOLUME /app/log

USER appuser
EXPOSE 10091

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]