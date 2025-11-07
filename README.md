# Micronaut Redis REST API Application

A REST API service built with Micronaut 4.7.3, Java 21, and Redis using Lettuce client.

## Prerequisites

- Java 21
- Docker (for running Redis)

## Getting Started

### 1. Start Redis with Docker

Run the following command to start a Redis container:

```bash
docker run --name redis -p 6379:6379 -d redis:latest
```

To verify Redis is running:

```bash
docker ps
```

To stop Redis:

```bash
docker stop redis
```

To remove the Redis container:

```bash
docker rm redis
```

### 2. Build the Application

```bash
./gradlew build
```

For Windows:

```bash
gradlew.bat build
```

### 3. Run the Application

```bash
./gradlew run
```

For Windows:

```bash
gradlew.bat run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Health Check

```bash
curl http://localhost:8080/api/items/health
```

### Create Item

```bash
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "id": "1",
    "name": "Sample Item",
    "description": "This is a sample item"
  }'
```

### Get Item

```bash
curl http://localhost:8080/api/items/1
```

### Update Item

```bash
curl -X PUT http://localhost:8080/api/items/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Item",
    "description": "This is an updated item"
  }'
```

### Delete Item

```bash
curl -X DELETE http://localhost:8080/api/items/1
```

## Project Structure

```
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── Application.java          # Main application class
│   │   │   ├── controller/
│   │   │   │   └── ItemController.java   # REST API controller
│   │   │   ├── service/
│   │   │   │   └── ItemService.java      # Redis service
│   │   │   └── model/
│   │   │       └── Item.java             # Item model
│   │   └── resources/
│   │       ├── application.yml           # Application configuration
│   │       └── logback.xml               # Logging configuration
├── build.gradle                          # Gradle build configuration
├── settings.gradle                       # Gradle settings
└── gradle/                               # Gradle wrapper files
```

## Configuration

The Redis connection is configured in `src/main/resources/application.yml`:

```yaml
redis:
  uri: redis://localhost:6379
```

To connect to a different Redis instance, update the URI in the configuration file.

## Technologies Used

- **Micronaut**: 4.7.3
- **Java**: 21
- **Gradle**: 8.11.1
- **Redis Client**: micronaut-redis-lettuce
- **Serialization**: micronaut-serde-jackson

## Development

### Running Tests

```bash
./gradlew test
```

### Clean Build

```bash
./gradlew clean build
```

### Creating a Fat JAR

```bash
./gradlew shadowJar
```

The fat JAR will be created in `build/libs/` directory and can be run with:

```bash
java -jar build/libs/micronaut-redis-app-0.1-all.jar
```
