package com.example.config;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import io.micronaut.configuration.lettuce.AbstractRedisConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;


/**
 * Configuration for Azure Redis with Managed Identity authentication.
 * It enables Managed Identity authentication for Azure Cache for Redis and Azure Managed Redis.
 * <p>
 * This configuration uses a {@link BeanCreatedEventListener} to intercept the creation of
 * {@link AbstractRedisConfiguration} and inject a {@link RedisCredentialsProvider} that uses
 * Azure Managed Identity for authentication. This approach preserves all existing
 * Micronaut Redis Lettuce features including:
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
 *   <li>The Managed Identity must have the authority to access Redis (e.g., "Redis Cache Data Owner" or "Redis Cache Data Contributor" role).</li>
 *   <li>Environment variable/property {@code azure.redis.username} should be set to the Object ID of the Managed Identity.</li>
 * </ul>
 * <p>
 * <strong>Configuration Example:</strong>
 * <pre>
 * redis:
 *   uri: rediss://example.redis.cache.windows.net:6380
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
 * @see BeanCreatedEventListener
 */
@Singleton
@Requires(property = "azure.redis.username")
public class AzureRedisCredentialsConfiguration implements BeanCreatedEventListener<AbstractRedisConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(AzureRedisCredentialsConfiguration.class);
    private static final String REDIS_SCOPE = "https://redis.azure.com/.default";

    private final String username;
    private final DefaultAzureCredential credential;
    private final TokenRequestContext tokenContext;

    public AzureRedisCredentialsConfiguration(@Value("${azure.redis.username}") String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Azure Redis username must be set and non-empty (property: azure.redis.username). " +
                    "This should be the Object ID of your Managed Identity or Service Principal.");
        }
        this.username = username.trim();
        this.credential = new DefaultAzureCredentialBuilder().build();
        this.tokenContext = new TokenRequestContext().addScopes(REDIS_SCOPE);
        LOG.info("Initialized Azure Managed Identity credentials provider for Redis authentication with username: {}", this.username);
    }

    @Override
    public AbstractRedisConfiguration onCreated(BeanCreatedEvent<AbstractRedisConfiguration> event) {
        AbstractRedisConfiguration config = event.getBean();

        // Set credentials provider on the RedisURI if present
        config.getUri().ifPresent(uri -> {
            uri.setCredentialsProvider(this::resolveAzureCredentials);
            LOG.info("Injected Azure Managed Identity credentials provider into RedisURI: {}", uri.getHost());
        });

        // Also set on the configuration itself (as it extends RedisURI)
        config.setCredentialsProvider(this::resolveAzureCredentials);
        LOG.info("Injected Azure Managed Identity credentials provider into Redis configuration");

        return config;
    }

    private Mono<RedisCredentials> resolveAzureCredentials() {
        return Mono.defer(() -> {
            LOG.debug("Resolving Azure Managed Identity credentials for Redis");
            return credential.getToken(tokenContext)
                    .doOnNext(token -> LOG.debug("Successfully obtained/refreshed Azure access token for Redis authentication"))
                    .map(token -> RedisCredentials.just(username, token.getToken()));
        });
    }

}