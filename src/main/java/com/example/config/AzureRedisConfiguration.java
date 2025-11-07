package com.example.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.protocol.ProtocolVersion;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class AzureRedisConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AzureRedisConfiguration.class);
    private static final String REDIS_SCOPE = "https://redis.azure.com/.default";

    @Bean
    @Context
    @Primary
    @Replaces(RedisClient.class)
    @Requires(property = "redis.uri")
    public RedisClient redisClient(@Value("${redis.uri}") String redisUri,
                                   @Value("${redis.username}") String username) {
        try {
            RedisURI uri = RedisURI.create(redisUri);

            // Obtain Azure access token using Managed Identity
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            TokenRequestContext tokenContext = new TokenRequestContext().addScopes(REDIS_SCOPE);
            AccessToken token = credential.getToken(tokenContext).block();

            LOG.info("Successfully obtained Azure access token for Redis authentication");

            // Configure Redis URI with Managed Identity authentication
            // Username should be the Object ID of the Managed Identity
            RedisURI authenticatedUri = RedisURI.Builder
                .redis(uri.getHost(), uri.getPort())
                .withAuthentication(username, token.getToken())
                .withSsl(true)
                .build();

            RedisClient client = RedisClient.create(authenticatedUri);

            // Use RESP3 protocol for Azure Entra ID authentication
            client.setOptions(ClientOptions.builder()
                .protocolVersion(ProtocolVersion.RESP3)
                .build());

            LOG.info("Redis client configured with Managed Identity authentication");

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
        return redisClient.connect();
    }
}
