# Micronaut Redis REST API Application

A REST API service built with Micronaut 4.7.3, Java 21, and Azure Cache for Redis using Managed Identity authentication.

## Prerequisites

- Java 21
- Azure Cache for Redis or Azure Managed Redis instance
- Azure Managed Identity or Service Principal configured with appropriate RBAC roles

## Getting Started

### 1. Setup Azure Redis and Managed Identity

#### Create Azure Cache for Redis

```bash
# Create resource group
az group create --name myResourceGroup --location eastus

# Create Azure Cache for Redis
az redis create --resource-group myResourceGroup \
  --name myRedisCache \
  --location eastus \
  --sku Basic \
  --vm-size c0 \
  --enable-non-ssl-port false
```

#### Configure Managed Identity

If running on Azure (App Service, Container Apps, AKS, etc.):
1. Enable system-assigned managed identity for your Azure service
2. Assign appropriate Redis RBAC role to the managed identity:

```bash
# Get the managed identity Object ID
IDENTITY_ID=$(az <resource-type> identity show --resource-group myResourceGroup --name myAppService --query principalId -o tsv)

# Assign Redis Data Contributor role
az role assignment create --assignee $IDENTITY_ID \
  --role "Redis Cache Contributor" \
  --scope /subscriptions/<subscription-id>/resourceGroups/myResourceGroup/providers/Microsoft.Cache/Redis/myRedisCache
```

For local development, use Azure CLI credentials or set up a service principal.

### 2. Configure Environment Variables

Set the following environment variables:

```bash
# Azure Redis connection string (use SSL port 6380)
export AZURE_REDIS_URI="rediss://myRedisCache.redis.cache.windows.net:6380"

# Object ID of your Managed Identity or Service Principal
export AZURE_MANAGED_IDENTITY_OBJECT_ID="<your-managed-identity-object-id>"
```

For Windows PowerShell:

```powershell
$env:AZURE_REDIS_URI="rediss://myRedisCache.redis.cache.windows.net:6380"
$env:AZURE_MANAGED_IDENTITY_OBJECT_ID="<your-managed-identity-object-id>"
```

### 3. Build the Application

```bash
./gradlew build
```

For Windows:

```bash
gradlew.bat build
```

### 4. Run the Application

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
  # Azure Cache for Redis URI with SSL (port 6380)
  uri: ${AZURE_REDIS_URI}
  # Managed Identity Object ID or Service Principal Object ID
  username: ${AZURE_MANAGED_IDENTITY_OBJECT_ID}
```

The application uses Azure Managed Identity for authentication, configured in `AzureRedisConfiguration.java`:
- Automatic token refresh using `DefaultAzureCredential`
- RESP3 protocol for Microsoft Entra ID authentication
- SSL/TLS connection to Azure Redis

## Technologies Used

- **Micronaut**: 4.7.3
- **Java**: 21
- **Gradle**: 8.11.1
- **Redis Client**: micronaut-redis-lettuce
- **Azure Identity**: com.azure:azure-identity:1.14.2
- **Serialization**: micronaut-serde-jackson
- **Authentication**: Azure Managed Identity with automatic token refresh

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
