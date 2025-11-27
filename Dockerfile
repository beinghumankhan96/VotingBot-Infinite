FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache chromium nss freetype harfbuzz ca-certificates ttf-freefont xvfb
ENV CHROME_BIN=/usr/bin/chromium-browser
WORKDIR /app
COPY --from=builder /app/target/*.jar .
CMD ["sh", "-c", "xvfb-run --server-args='-screen 0 1920x1080x24' java -jar infinite-voting-bot-1.0.jar"]
