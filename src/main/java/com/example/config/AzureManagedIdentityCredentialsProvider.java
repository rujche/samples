package com.example.config;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Credentials provider for Azure Redis with Managed Identity authentication.
 * It enables Managed Identity authentication for Azure Cache for Redis and Azure Managed Redis.
 * <p>
 * This provider integrates with Micronaut Redis Lettuce's existing configuration mechanism
 * by providing a {@link RedisCredentialsProvider} bean that can be referenced in the
 * {@code redis.credentials-provider} property. This approach preserves all existing
 * Micronaut Redis features including:
 * <ul>
 *   <li>Connection pooling configuration ({@code redis.pool.*})</li>
 *   <li>Master-replica setup ({@code redis.replica-uris})</li>
 *   <li>Read-from configuration ({@code redis.read-from})</li>
 *   <li>Multiple Redis servers ({@code redis.servers.*})</li>
 *   <li>Client resources configuration (IO/computation thread pools)</li>
 * </ul>
 * <p>
 * The provider uses {@link DefaultAzureCredential} to obtain access tokens from Azure Active Directory
 * and automatically refreshes tokens before they expire.
 * <p>
 * <strong>Prerequisites:</strong>
 * <ul>
 *   <li>The Managed Identity must have the authority to access Redis (e.g., "Redis Cache Contributor" role).</li>
 *   <li>Environment variable/property {@code redis.credentials-provider} must be set to "managedIdentityCredentialsProvider".</li>
 *   <li>Environment variable/property {@code azure.redis.username} should be set to the Object ID of the Managed Identity.</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 * <pre>
 * redis:
 *   uri: rediss://example.redis.cache.windows.net:6380
 *   credentials-provider: managedIdentityCredentialsProvider
 *   pool:
 *     enabled: true
 *     max-total: 8
 * azure:
 *   redis:
 *     username: ${AZURE_MANAGED_IDENTITY_OBJECT_ID}
 * </pre>
 *
 * @see DefaultAzureCredential
 * @see RedisCredentialsProvider
 */
@Singleton
@Named("managedIdentityCredentialsProvider")
@Requires(property = "azure.redis.username")
public class AzureManagedIdentityCredentialsProvider implements RedisCredentialsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AzureManagedIdentityCredentialsProvider.class);
    private static final String REDIS_SCOPE = "https://redis.azure.com/.default";

    private final String username;
    private final DefaultAzureCredential credential;
    private final TokenRequestContext tokenContext;

    public AzureManagedIdentityCredentialsProvider(@Value("${azure.redis.username}") String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Azure Redis username must be set and non-empty (property: azure.redis.username). " +
                    "This should be the Object ID of your Managed Identity or Service Principal.");
        }
        this.username = username;
        this.credential = new DefaultAzureCredentialBuilder().build();
        this.tokenContext = new TokenRequestContext().addScopes(REDIS_SCOPE);
        LOG.info("Initialized Azure Managed Identity credentials provider for Redis authentication with username: {}", username);
    }

    @Override
    public Mono<RedisCredentials> resolveCredentials() {
        return Mono.defer(() -> {
            LOG.debug("Resolving Azure Managed Identity credentials for Redis");
            return credential.getToken(tokenContext)
                    .doOnNext(token -> LOG.debug("Successfully obtained/refreshed Azure access token for Redis authentication"))
                    .map(token -> RedisCredentials.just(username, token.getToken()))
                    .onErrorMap(e -> {
                        // Distinguish between retryable and non-retryable errors
                        String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                        String errorType = e.getClass().getSimpleName();

                        // Check for permission/authorization errors (non-retryable)
                        if (errorMessage.contains("unauthorized") ||
                                errorMessage.contains("forbidden") ||
                                errorMessage.contains("access denied") ||
                                errorMessage.contains("permission") ||
                                errorMessage.contains("not authorized") ||
                                errorType.contains("CredentialUnavailable")) {

                            LOG.error("Permission error while obtaining Azure access token for Redis authentication. " +
                                    "The Managed Identity does not have the necessary permissions. " +
                                    "Please ensure the Managed Identity has 'Redis Cache Contributor' role or appropriate permissions.", e);
                            return new SecurityException(
                                    "Permission denied: The Managed Identity does not have authority to access Azure Redis. " +
                                            "Please assign the 'Redis Cache Contributor' role or appropriate permissions to the Managed Identity with Object ID: " + username, e);
                        }

                        // Check for network/connectivity errors (retryable)
                        if (errorMessage.contains("timeout") ||
                                errorMessage.contains("connection") ||
                                errorMessage.contains("network") ||
                                errorMessage.contains("connect timed out") ||
                                errorMessage.contains("unreachable") ||
                                errorType.contains("IOException") ||
                                errorType.contains("ConnectException") ||
                                errorType.contains("SocketTimeoutException")) {

                            LOG.warn("Network error while obtaining Azure access token for Redis authentication. " +
                                    "This is a transient error and may be retried.", e);
                            return new java.io.IOException(
                                    "Network error while obtaining Azure access token: " + e.getMessage() +
                                            ". This is a transient error that can be retried.", e);
                        }

                        // Default: treat as retryable but log as error
                        LOG.error("Failed to obtain Azure access token for Redis authentication. Error type: {}. This may be retryable.", errorType, e);
                        return new RuntimeException(
                                "Failed to obtain Azure access token for Redis authentication: " + e.getMessage(), e);
                    });
        });
    }
}