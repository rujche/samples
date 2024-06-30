# Build stage
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS build
WORKDIR /app
COPY mvnw* /app/
COPY .mvn /app/.mvn
COPY pom.xml /app
COPY ./src /app/src
RUN chmod +x ./mvnw
RUN ./mvnw package


# Runtime stage
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
COPY --from=build /app/target/*.jar /usr/src/myapp/app.jar
CMD ["/usr/bin/java", "-jar", "/usr/src/myapp/app.jar"]