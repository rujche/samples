# Fix for NOAUTH Authentication Error

## Problem

The application failed with the following error:

```
RedisConnectionException: Unable to connect to amr-20251110.westus2.redis.azure.net/<unresolved>:10000

Caused by: io.lettuce.core.RedisCommandExecutionException: NOAUTH HELLO must be called with the client already authenticated, 
otherwise the HELLO AUTH <user> <pass> option can be used to authenticate the client and select the RESP protocol version at the same time
```

## Root Cause

The credentials provider was being set on the **`AbstractRedisConfiguration`** object, but **not** on the actual **`RedisURI`** object that Lettuce uses to connect to Redis.

### The Problem Explained

`AbstractRedisConfiguration` has a complex structure:

```java
public abstract class AbstractRedisConfiguration extends RedisURI {
    private RedisURI uri;  // ← Separate URI field
    // ...
}
```

**Two separate things:**
1. **`uri` field** - A separate `RedisURI` object (set when you use `redis.uri` property)
2. **Extends `RedisURI`** - The configuration itself IS a `RedisURI` (used when you set individual properties like `redis.host`, `redis.port`)

### What Was Happening

**Before the fix:**
```java
@Override
public AbstractRedisConfiguration onCreated(BeanCreatedEvent<AbstractRedisConfiguration> event) {
    AbstractRedisConfiguration config = event.getBean();
    // Only set on the configuration itself
    config.setCredentialsProvider(this::resolveAzureCredentials);  // ❌ Wrong!
    return config;
}
```

**Flow:**
1. User sets `redis.uri` in application.yml
2. Micronaut creates `AbstractRedisConfiguration`
3. Micronaut calls `config.setUri()` which creates a **new `RedisURI` object** in the `uri` field
4. Our listener sets credentials provider on `config` (which extends `RedisURI`)
5. Lettuce tries to connect using `config.getUri()` → returns the **`uri` field** (which has **no credentials provider**)
6. Connection fails with NOAUTH error

### The Fix

**After the fix:**
```java
@Override
public AbstractRedisConfiguration onCreated(BeanCreatedEvent<AbstractRedisConfiguration> event) {
    AbstractRedisConfiguration config = event.getBean();
    
    // Set credentials provider on the RedisURI if present
    config.getUri().ifPresent(uri -> {
        uri.setCredentialsProvider(this::resolveAzureCredentials);  // ✅ Set on URI field
        LOG.info("Injected Azure Managed Identity credentials provider into RedisURI: {}", uri.getHost());
    });
    
    // Also set on the configuration itself (as it extends RedisURI)
    config.setCredentialsProvider(this::resolveAzureCredentials);  // ✅ Set on config too
    LOG.info("Injected Azure Managed Identity credentials provider into Redis configuration");
    
    return config;
}
```

**Flow:**
1. User sets `redis.uri` in application.yml
2. Micronaut creates `AbstractRedisConfiguration`
3. Micronaut calls `config.setUri()` which creates a new `RedisURI` object in the `uri` field
4. Our listener:
   - Gets the `uri` field via `config.getUri()`
   - Sets credentials provider on it ✅
   - Also sets credentials provider on `config` itself (for fallback)
5. Lettuce tries to connect using `config.getUri()` → returns the `uri` field with credentials provider ✅
6. Connection succeeds! 🎉

## Why Set on Both?

```java
// Set on the URI field (when redis.uri is used)
config.getUri().ifPresent(uri -> {
    uri.setCredentialsProvider(this::resolveAzureCredentials);
});

// Set on the configuration itself (when redis.host/port is used)
config.setCredentialsProvider(this::resolveAzureCredentials);
```

**Reason:**
- If user uses `redis.uri`, Lettuce uses the `uri` field
- If user uses `redis.host` and `redis.port`, Lettuce uses the configuration object itself
- We set on both to handle all cases

## Configuration Scenarios

### Scenario 1: Using `redis.uri` (Most Common)

```yaml
redis:
  uri: rediss://example.redis.cache.windows.net:6380
```

**What happens:**
- `config.getUri()` returns the `uri` field with credentials provider ✅
- Lettuce connects using this URI

### Scenario 2: Using Individual Properties

```yaml
redis:
  host: example.redis.cache.windows.net
  port: 6380
  ssl: true
```

**What happens:**
- `config.getUri()` returns `Optional.empty()`
- Lettuce uses the configuration object itself (which has credentials provider) ✅

### Scenario 3: Multiple Redis Servers

```yaml
redis:
  servers:
    server1:
      uri: rediss://cache1.redis.azure.net:6380
    server2:
      uri: rediss://cache2.redis.azure.net:6380
```

**What happens:**
- Listener is called for each `AbstractRedisConfiguration` bean
- Each gets its own credentials provider
- Both servers authenticate correctly ✅

## Verification

### Check Logs

After starting the application, you should see:

```
INFO: Initialized Azure Managed Identity credentials provider for Redis authentication with username: <object-id>
INFO: Injected Azure Managed Identity credentials provider into RedisURI: amr-20251110.westus2.redis.azure.net
INFO: Injected Azure Managed Identity credentials provider into Redis configuration
```

### Check Connection

When connecting to Redis:
```
DEBUG: Resolving Azure Managed Identity credentials for Redis
DEBUG: Successfully obtained/refreshed Azure access token for Redis authentication
```

### No NOAUTH Error

The application should no longer see:
```
❌ NOAUTH HELLO must be called with the client already authenticated
```

## Testing the Fix

### Test 1: Verify Credentials Provider is Set

Add debug logging to see what's happening:

```java
@Override
public AbstractRedisConfiguration onCreated(BeanCreatedEvent<AbstractRedisConfiguration> event) {
    AbstractRedisConfiguration config = event.getBean();
    
    LOG.info("Configuration class: {}", config.getClass().getName());
    LOG.info("Has URI field: {}", config.getUri().isPresent());
    
    config.getUri().ifPresent(uri -> {
        LOG.info("URI host: {}", uri.getHost());
        LOG.info("URI port: {}", uri.getPort());
        uri.setCredentialsProvider(this::resolveAzureCredentials);
        LOG.info("Credentials provider set on URI field");
    });
    
    config.setCredentialsProvider(this::resolveAzureCredentials);
    LOG.info("Credentials provider set on configuration");
    
    return config;
}
```

### Test 2: Verify Token Retrieval

Test that tokens are being fetched:

```java
private Mono<RedisCredentials> resolveAzureCredentials() {
    return Mono.defer(() -> {
        LOG.info("===== FETCHING AZURE TOKEN =====");
        LOG.info("Username (Object ID): {}", username);
        
        return credential.getToken(tokenContext)
            .doOnNext(token -> {
                LOG.info("Token obtained successfully");
                LOG.info("Token expires at: {}", token.getExpiresAt());
            })
            .map(token -> {
                RedisCredentials creds = RedisCredentials.just(username, token.getToken());
                LOG.info("Created RedisCredentials with username: {}", username);
                return creds;
            })
            .onErrorMap(e -> {
                LOG.error("FAILED to get token: {}", e.getMessage(), e);
                // ... error handling
            });
    });
}
```

## Common Issues and Solutions

### Issue 1: Still Getting NOAUTH Error

**Possible causes:**
1. ❌ The `config.getUri().ifPresent()` block isn't being executed
2. ❌ The property `azure.redis.username` is not set

**Solution:**
- Check logs for "Injected Azure Managed Identity credentials provider into RedisURI"
- Verify `azure.redis.username` is set in application.yml
- Check that the listener is being created (look for "Initialized Azure Managed Identity credentials provider")

### Issue 2: Credentials Provider Not Called

**Possible causes:**
1. ❌ Credentials provider was set after connection was already established
2. ❌ Listener wasn't executed

**Solution:**
- Ensure `@Requires(property = "azure.redis.username")` is present
- Verify the property is set before application starts
- Check bean creation order

### Issue 3: Token Fetch Fails

**Possible causes:**
1. ❌ Managed Identity not configured
2. ❌ No network access to Azure AD

**Solution:**
- Test locally with `az login` and Azure CLI
- In Azure, ensure Managed Identity is enabled
- Check firewall/network settings

## Technical Details

### AbstractRedisConfiguration Structure

```java
public abstract class AbstractRedisConfiguration extends RedisURI {
    // Field: separate URI object
    private RedisURI uri;
    
    // Getter returns the field if present
    public Optional<RedisURI> getUri() {
        if (uri != null) {
            uri.setClientName(getClientName());
        }
        return Optional.ofNullable(uri);
    }
    
    // Setter creates new RedisURI object
    public void setUri(java.net.URI uri) {
        this.uri = RedisURI.create(uri);
    }
    
    // Inherited from RedisURI
    public void setCredentialsProvider(RedisCredentialsProvider provider) {
        // Sets on this object (the configuration itself)
    }
}
```

### RedisClient Connection Logic

When creating a connection, Lettuce does:

```java
// In AbstractRedisClientFactory
public RedisClient redisClient(AbstractRedisConfiguration config) {
    Optional<RedisURI> uri = config.getUri();
    
    return uri
        .map(RedisClient::create)  // ← Uses the URI field if present
        .orElseGet(() -> RedisClient.create(config));  // ← Uses config itself
}
```

So if `redis.uri` is set, it uses the **`uri` field**, not the configuration object!

### Why This Matters

The credentials provider must be on the **exact `RedisURI` object** that Lettuce uses to connect:

```java
// ❌ WRONG: Set on config, but Lettuce uses config.getUri()
config.setCredentialsProvider(provider);
RedisClient.create(config.getUri().get());  // This URI doesn't have provider!

// ✅ CORRECT: Set on the URI that Lettuce will use
config.getUri().get().setCredentialsProvider(provider);
RedisClient.create(config.getUri().get());  // This URI has provider!
```

## Summary

### The Fix

Set credentials provider on **both**:
1. The `uri` field (via `config.getUri().ifPresent()`)
2. The configuration object itself (via `config.setCredentialsProvider()`)

### Why It Works

- Handles the `redis.uri` scenario (uses URI field)
- Handles the `redis.host`/`redis.port` scenario (uses config object)
- Works with multiple Redis servers
- Works with master-replica configuration

### Files Updated

1. ✅ `AzureRedisCredentialsConfiguration.java` - Fixed `onCreated()` method
2. ✅ `application.yml` - Fixed YAML indentation
3. ✅ `auth-by-mi-for-azure-redis-in-micronaut-project.md` - Updated migration guide

The application should now connect successfully to Azure Redis with Managed Identity authentication! 🎉

