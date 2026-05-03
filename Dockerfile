
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew build --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV AI_API_KEY=""
ENV GOOGLE_GEMINI_MODEL="gemini-1.5-flash"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]