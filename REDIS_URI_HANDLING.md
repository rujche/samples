# Redis URI Handling in Micronaut Redis Lettuce

This document explains how the `redis.uri` property from `application.yml` is handled in the Micronaut Redis Lettuce module.

## Overview

The `redis.uri` property is processed through a chain of configuration classes in the `micronaut-redis-lettuce` dependency (version 6.6.1 in your project).

## Configuration Flow

### 1. Application Configuration (`application.yml`)

```yaml
redis:
  uri: redis://localhost:6379
```

### 2. Configuration Classes

#### `AbstractRedisConfiguration` 
**Package**: `io.micronaut.configuration.lettuce`

This is the base configuration class that holds the `redis.uri` property.

**Key properties:**
```java
private RedisURI uri;
private List<RedisURI> uris;
private List<RedisURI> replicaUris;
private Integer ioThreadPoolSize;
private Integer computationThreadPoolSize;
private String name;
private ReadFrom readFrom;
```

**Key methods:**
```java
// Getter for URI - returns Optional
public Optional<RedisURI> getUri() {
    if (uri != null) {
        uri.setClientName(getClientName());
    }
    return Optional.ofNullable(uri);
}

// Setter - converts java.net.URI to io.lettuce.core.RedisURI
public void setUri(java.net.URI uri) {
    this.uri = RedisURI.create(uri);
}
```

**Important**: The setter accepts `java.net.URI` and converts it to Lettuce's `RedisURI` using `RedisURI.create(uri)`.

#### `DefaultRedisConfiguration`
**Package**: `io.micronaut.configuration.lettuce`

This class extends `AbstractRedisConfiguration` and is annotated with `@ConfigurationProperties(prefix = "redis")`, which means Micronaut automatically binds properties starting with `redis.` to this class.

```java
@ConfigurationProperties("redis")
public class DefaultRedisConfiguration extends AbstractRedisConfiguration {
    public DefaultRedisConfiguration() {
        super();
    }
}
```

### 3. Client Factory

#### `AbstractRedisClientFactory`
**Package**: `io.micronaut.configuration.lettuce`

This factory creates the Redis client using the configuration:

```java
public RedisClient redisClient(AbstractRedisConfiguration config) {
    Optional<RedisURI> uri = config.getUri();
    
    return uri
        .map(RedisClient::create)  // If URI present, use it
        .orElseGet(() -> RedisClient.create(config));  // Otherwise use config object
}
```

#### `DefaultRedisClientFactory`
**Package**: `io.micronaut.configuration.lettuce`

This factory extends `AbstractRedisClientFactory` and creates connections:

```java
@Factory
public class DefaultRedisClientFactory<K, V> extends AbstractRedisClientFactory<K, V> {
    
    @Bean
    @Singleton
    public StatefulRedisConnection<K, V> redisConnection(
            RedisClient redisClient, 
            AbstractRedisConfiguration config) {
        
        // Check if URI and replica URIs are configured for master-replica setup
        if (config.getUri().isPresent() && !config.getReplicaUris().isEmpty()) {
            List<RedisURI> uris = new ArrayList<>(config.getReplicaUris());
            uris.add(config.getUri().get());
            
            StatefulRedisMasterReplicaConnection<K, V> connection = 
                MasterReplica.connect(redisClient, defaultCodec, uris);
            
            if (config.getReadFrom().isPresent()) {
                connection.setReadFrom(config.getReadFrom().get());
            }
            return connection;
        }
        
        // Simple connection
        return super.redisConnection(redisClient, defaultCodec);
    }
}
```

### 4. Usage in Your Application

In your `ItemService.java`, the `StatefulRedisConnection` is automatically injected:

```java
@Singleton
public class ItemService {
    private final RedisCommands<String, String> commands;
    
    public ItemService(StatefulRedisConnection<String, String> connection,
                      ObjectSerializer objectSerializer) {
        this.commands = connection.sync();
        this.objectSerializer = objectSerializer;
    }
    
    // ... your Redis operations
}
```

## How It Works

1. **Micronaut starts** and scans for `@ConfigurationProperties` beans
2. **`DefaultRedisConfiguration`** is created and the `redis.uri` value from `application.yml` is bound to it via the `setUri()` method
3. **`DefaultRedisClientFactory`** creates a `RedisClient` bean using the configuration
4. **`StatefulRedisConnection`** bean is created from the `RedisClient`
5. **Your `ItemService`** injects the `StatefulRedisConnection` and uses it

## Configuration Metadata

From `spring-configuration-metadata.json`:

```json
{
  "name": "redis.uri",
  "type": "java.net.URI",
  "sourceType": "io.micronaut.configuration.lettuce.AbstractRedisConfiguration",
  "description": ""
}
```

## Alternative Configuration

Instead of using `redis.uri`, you can also configure individual properties:

```yaml
redis:
  host: localhost
  port: 6379
  password: mypassword
  database: 0
  ssl: false
  timeout: 60s
```

These properties are also handled by `AbstractRedisConfiguration` (which extends `io.lettuce.core.RedisURI`).

## Multiple Redis Servers

You can configure multiple Redis servers using:

```yaml
redis:
  servers:
    server1:
      uri: redis://host1:6379
    server2:
      uri: redis://host2:6379
```

This is handled by `NamedRedisServersConfiguration`.

## Source Code Reference

The actual source code for these classes can be found in the Micronaut Redis repository:
- **GitHub**: https://github.com/micronaut-projects/micronaut-redis
- **Package**: `redis-lettuce/src/main/java/io/micronaut/configuration/lettuce/`

## Key Classes Summary

| Class | Purpose |
|-------|---------|
| `AbstractRedisConfiguration` | Base configuration class that holds redis.uri and other properties |
| `DefaultRedisConfiguration` | Concrete implementation bound to `redis.*` properties |
| `AbstractRedisClientFactory` | Creates RedisClient instances from configuration |
| `DefaultRedisClientFactory` | Creates StatefulRedisConnection beans |
| `io.lettuce.core.RedisURI` | Lettuce's URI class (parent of AbstractRedisConfiguration) |
| `io.lettuce.core.RedisClient` | Lettuce's Redis client |

## Build Dependencies

Your project uses:
```gradle
implementation("io.micronaut.redis:micronaut-redis-lettuce")
```

This pulls in:
- `micronaut-redis-lettuce-6.6.1.jar` (the configuration and factory classes)
- `lettuce-core-6.4.0.RELEASE.jar` (the actual Redis client library)

