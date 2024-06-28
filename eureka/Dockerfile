FROM openjdk:21-jdk-slim

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew

RUN ./gradlew build

CMD ["java", "-jar", "build/libs/eureka-0.0.1-SNAPSHOT.jar"]

EXPOSE 8761