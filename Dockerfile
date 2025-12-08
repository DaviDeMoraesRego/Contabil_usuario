FROM eclipse-temurin:17-jdk-alpine AS builder
RUN mkdir -p /build
WORKDIR /build
COPY pom.xml /build
COPY mvnw /build/mvnw
COPY .mvn /build/.mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve dependency:resolve-plugins || true
COPY src /build/src
RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runtime
EXPOSE 10091
ENV APP_HOME=/contabil-game
ENV JAVA_OPTS=""
RUN mkdir -p $APP_HOME
RUN mkdir -p $APP_HOME/config
RUN mkdir -p $APP_HOME/log
VOLUME $APP_HOME/log
VOLUME $APP_HOME/config
WORKDIR $APP_HOME
COPY --from=builder /build/target/contabil-game-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]