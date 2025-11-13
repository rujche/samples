# Enhanced Error Handling for Azure Redis Managed Identity Authentication

## Overview

The `AzureManagedIdentityCredentialsProvider` now includes intelligent error handling that distinguishes between **retryable** and **non-retryable** errors, allowing the Redis client to make better decisions about whether to retry a failed authentication attempt or abort immediately.

## Error Categories

### 1. **Non-Retryable Errors** → `SecurityException`

These errors indicate permission/authorization problems that won't be resolved by retrying:

**Detection criteria:**
- Error message contains: `"unauthorized"`, `"forbidden"`, `"access denied"`, `"permission"`, `"not authorized"`
- Error type contains: `"CredentialUnavailable"`

**Response:**
- Logs as **ERROR** level
- Throws `SecurityException` with detailed message including the Managed Identity Object ID
- Redis client should **ABORT** and not retry

**Example scenarios:**
- Managed Identity doesn't have "Redis Cache Contributor" role
- Managed Identity Object ID is incorrect
- Azure AD token request is rejected due to missing permissions
- Credential chain exhausted without finding valid credentials

**Log message:**
```
ERROR: Permission error while obtaining Azure access token for Redis authentication. 
The Managed Identity does not have the necessary permissions. 
Please ensure the Managed Identity has 'Redis Cache Contributor' role or appropriate permissions.
```

**Exception message:**
```
Permission denied: The Managed Identity does not have authority to access Azure Redis. 
Please assign the 'Redis Cache Contributor' role or appropriate permissions to the Managed Identity with Object ID: <object-id>
```

### 2. **Retryable Errors** → `IOException`

These errors indicate transient network/connectivity issues that may succeed on retry:

**Detection criteria:**
- Error message contains: `"timeout"`, `"connection"`, `"network"`, `"connect timed out"`, `"unreachable"`
- Error type contains: `"IOException"`, `"ConnectException"`, `"SocketTimeoutException"`

**Response:**
- Logs as **WARN** level (not ERROR, since it's expected to be transient)
- Throws `IOException` with message indicating it's retryable
- Redis client should **RETRY** the operation

**Example scenarios:**
- Azure AD endpoint temporarily unavailable
- Network connectivity issues
- DNS resolution failures
- Connection timeout due to high latency
- Temporary Azure service outage

**Log message:**
```
WARN: Network error while obtaining Azure access token for Redis authentication. 
This is a transient error and may be retried.
```

**Exception message:**
```
Network error while obtaining Azure access token: <original-error-message>. 
This is a transient error that can be retried.
```

### 3. **Unknown Errors** → `RuntimeException`

Any other errors are treated as potentially retryable but logged as errors:

**Response:**
- Logs as **ERROR** level
- Throws `RuntimeException` with original error message
- Redis client may retry based on its own retry policy

**Log message:**
```
ERROR: Failed to obtain Azure access token for Redis authentication. 
Error type: <error-type>. This may be retryable.
```

## How Lettuce Handles These Errors

Lettuce Redis client has built-in retry logic for `RedisCredentialsProvider`:

1. **`SecurityException` (Non-retryable)**
   - Lettuce will typically propagate this immediately
   - Connection attempt fails fast
   - Application should handle this as a configuration error

2. **`IOException` (Retryable)**
   - Lettuce may retry based on its reconnection strategy
   - Automatic backoff and retry logic applies
   - Connection will be retried until successful or max attempts reached

3. **`RuntimeException` (Unknown)**
   - Lettuce treats as potentially retryable
   - May retry a limited number of times
   - Eventually propagates if continues to fail

## Benefits

### 1. **Faster Failure for Configuration Errors**
- Permission errors don't waste time with multiple retry attempts
- Clear error messages help developers quickly identify the issue
- Includes the Object ID in the error message for easy troubleshooting

### 2. **Resilience for Transient Issues**
- Network issues automatically retry
- Temporary Azure AD outages don't cause permanent failures
- Application continues to work after transient issues resolve

### 3. **Better Observability**
- Different log levels (ERROR vs WARN) indicate severity
- Error types help with monitoring and alerting
- Detailed messages aid in troubleshooting

### 4. **Correct Exception Types**
- `SecurityException` signals authentication/authorization issues
- `IOException` signals network/connectivity issues
- Standard Java exception hierarchy for proper handling

## Testing Recommendations

### Test Non-Retryable Errors

```yaml
# Use an invalid Object ID to test permission errors
azure:
  redis:
    username: "00000000-0000-0000-0000-000000000000"
```

**Expected behavior:**
- Connection fails immediately
- `SecurityException` is thrown
- ERROR log with permission message
- No retry attempts

### Test Retryable Errors

```yaml
# Test with network issues (disconnect network temporarily)
redis:
  uri: rediss://example.redis.cache.windows.net:6380
  credentials-provider: managedIdentityCredentialsProvider
```

**Expected behavior:**
- Connection retry attempts
- WARN logs about network errors
- Eventually succeeds when network is restored
- Or fails after max retry attempts

## Implementation Details

### Error Detection Logic

```java
// Check error message (case-insensitive)
String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

// Check error type
String errorType = e.getClass().getSimpleName();

// Permission error keywords
if (errorMessage.contains("unauthorized") || 
    errorMessage.contains("forbidden") || 
    errorType.contains("CredentialUnavailable")) {
    // Non-retryable
}

// Network error keywords
if (errorMessage.contains("timeout") || 
    errorMessage.contains("connection") ||
    errorType.contains("IOException")) {
    // Retryable
}
```

### Exception Wrapping

The `onErrorMap()` operator in Reactor transforms exceptions:

```java
.onErrorMap(e -> {
    // Analyze error
    if (isPermissionError(e)) {
        return new SecurityException("Permission denied...", e);
    }
    if (isNetworkError(e)) {
        return new IOException("Network error...", e);
    }
    return new RuntimeException("Unknown error...", e);
})
```

## Monitoring Recommendations

### Key Metrics to Track

1. **Permission Errors** (`SecurityException` count)
   - Should be zero in production
   - Spike indicates configuration issue

2. **Network Errors** (`IOException` count)
   - Occasional errors are normal
   - Sustained increase indicates connectivity issues

3. **Successful Token Refreshes**
   - Should happen regularly (tokens expire hourly)
   - Track time to obtain token

### Alerting Suggestions

- **Critical Alert**: `SecurityException` in production
  - Indicates misconfiguration
  - Immediate action required

- **Warning Alert**: High rate of `IOException`
  - Indicates network instability
  - Investigate Azure AD connectivity

## Troubleshooting Guide

### "Permission denied" Error

1. Verify Managed Identity Object ID:
   ```bash
   az identity show --name <identity-name> --resource-group <rg-name> --query principalId
   ```

2. Verify role assignment:
   ```bash
   az role assignment list --assignee <object-id> --scope /subscriptions/<sub-id>/resourceGroups/<rg-name>/providers/Microsoft.Cache/redis/<redis-name>
   ```

3. Assign role if missing:
   ```bash
   az role assignment create --role "Redis Cache Contributor" --assignee <object-id> --scope /subscriptions/<sub-id>/resourceGroups/<rg-name>/providers/Microsoft.Cache/redis/<redis-name>
   ```

### "Network error" Alerts

1. Check Azure AD endpoint connectivity:
   ```bash
   curl -I https://login.microsoftonline.com
   ```

2. Check DNS resolution:
   ```bash
   nslookup login.microsoftonline.com
   ```

3. Review Azure service health:
   - https://status.azure.com

4. Check firewall rules and network security groups

## Code Location

- **Implementation**: `src/main/java/com/example/config/AzureManagedIdentityCredentialsProvider.java`
- **Configuration**: `src/main/resources/application.yml`
- **Error handling**: `resolveCredentials()` method, `onErrorMap()` operator

