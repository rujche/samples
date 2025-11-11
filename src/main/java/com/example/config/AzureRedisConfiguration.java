package com.example.config;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Factory
public class AzureRedisConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AzureRedisConfiguration.class);
    private static final String REDIS_SCOPE = "https://redis.azure.com/.default";

    private final DefaultAzureCredential credential;
    private final TokenRequestContext tokenContext;
    private volatile StatefulRedisConnection<String, String> connection;

    public AzureRedisConfiguration() {
        this.credential = new DefaultAzureCredentialBuilder().build();
        this.tokenContext = new TokenRequestContext().addScopes(REDIS_SCOPE);
    }

    @Bean
    @Context
    @Primary
    @Replaces(RedisClient.class)
    @Requires(property = "redis.uri")
    @Requires(property = "redis.username")
    public RedisClient redisClient(@Value("${redis.uri}") String redisUri,
                                    @Value("${redis.username}") String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Redis username must be set and non-empty (property: redis.username)");
        }
        try {
            RedisURI uri = RedisURI.create(redisUri);

            // Create credentials provider with automatic token refresh
            RedisCredentialsProvider credentialsProvider = new RedisCredentialsProvider() {
                @Override
                public Mono<RedisCredentials> resolveCredentials() {
                    return Mono.defer(() ->
                        credential.getToken(tokenContext)
                            .doOnNext(token -> LOG.debug("Obtained/refreshed Azure access token for Redis authentication"))
                            .map(token -> RedisCredentials.just(username, token.getToken()))
                    );
                }
            };

            // Configure Redis URI with credentials provider
            RedisURI authenticatedUri = RedisURI.Builder
                .redis(uri.getHost(), uri.getPort())
                .withSsl(uri.isSsl())
                .build();
            authenticatedUri.setCredentialsProvider(credentialsProvider);
            RedisClient client = RedisClient.create(authenticatedUri);
            LOG.info("Redis client configured with Managed Identity authentication and automatic token refresh");
            return client;
        } catch (Exception e) {
            LOG.error("Failed to configure Azure Redis client", e);
            throw new RuntimeException("Failed to configure Azure Redis client", e);
        }
    }

    @Bean
    @Singleton
    @Primary
    @Replaces(StatefulRedisConnection.class)
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        this.connection = redisClient.connect();
        return this.connection;
    }

    @PreDestroy
    public void cleanup() {
        if (connection != null && connection.isOpen()) {
            LOG.info("Closing Redis connection");
            connection.close();
        }
    }
}