# Micronaut Redis REST API Application

A REST API service built with Micronaut 4.7.3, Java 21, and Azure Cache for Redis with Managed Identity authentication using Lettuce client.

## Prerequisites

- Java 21
- Azure Cache for Redis or Azure Managed Redis instance
- Azure Managed Identity or Service Principal configured with access to Redis instance

## Getting Started

### 1. Set Up Azure Redis

This application uses Azure Cache for Redis with Managed Identity authentication. Ensure you have:

1. An Azure Cache for Redis or Azure Managed Redis instance created
2. A Managed Identity or Service Principal with appropriate permissions to access the Redis instance
3. The following environment variables configured:
   - `AZURE_REDIS_URI`: Your Azure Redis URI (e.g., `rediss://example.redis.cache.windows.net:6380`)
   - `AZURE_MANAGED_IDENTITY_OBJECT_ID`: The Object ID of your Managed Identity or Service Principal

**Note**: For local development with a local Redis instance, you can use:
```bash
docker run --name redis -p 6379:6379 -d redis:latest
```
And set environment variables:
```bash
export AZURE_REDIS_URI=redis://localhost:6379
export AZURE_MANAGED_IDENTITY_OBJECT_ID=local-dev-id
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

The Redis connection is configured in `src/main/resources/application.yml` using environment variables:

```yaml
redis:
  # Example Azure Cache for Redis uri: rediss://example.redis.cache.windows.net:6380
  uri: ${AZURE_REDIS_URI}
  # Username should be the Object ID of your Managed Identity or Service Principal
  username: ${AZURE_MANAGED_IDENTITY_OBJECT_ID}
```

The application uses Azure Managed Identity for authentication. The `AzureRedisConfiguration` class handles:
- Obtaining Azure access tokens using `DefaultAzureCredential`
- Configuring the Redis client with RESP3 protocol for Azure Entra ID authentication
- Establishing SSL/TLS connections to Azure Redis

Set the required environment variables before running the application:

```bash
export AZURE_REDIS_URI=rediss://your-redis-instance.redis.cache.windows.net:6380
export AZURE_MANAGED_IDENTITY_OBJECT_ID=your-managed-identity-object-id
```

## Technologies Used

- **Micronaut**: 4.7.3
- **Java**: 21
- **Gradle**: 8.11.1
- **Redis Client**: micronaut-redis-lettuce
- **Azure Identity**: 1.12.2 (for Managed Identity authentication)
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
