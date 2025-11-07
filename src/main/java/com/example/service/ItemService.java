package com.example.service;

import com.example.model.Item;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.micronaut.core.serialize.ObjectSerializer;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
public class ItemService {

    private final RedisCommands<String, String> commands;
    private final ObjectSerializer objectSerializer;

    public ItemService(StatefulRedisConnection<String, String> connection,
                      ObjectSerializer objectSerializer) {
        this.commands = connection.sync();
        this.objectSerializer = objectSerializer;
    }

    public void saveItem(Item item) {
        String key = "item:" + item.getId();
        String value = serializeItem(item);
        commands.set(key, value);
    }

    public Optional<Item> getItem(String id) {
        String key = "item:" + id;
        String value = commands.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(deserializeItem(value));
    }

    public void deleteItem(String id) {
        String key = "item:" + id;
        commands.del(key);
    }

    public boolean existsItem(String id) {
        String key = "item:" + id;
        return commands.exists(key) > 0;
    }

    private String serializeItem(Item item) {
        try {
            return new String(objectSerializer.serialize(item).orElseThrow());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize item", e);
        }
    }

    private Item deserializeItem(String value) {
        try {
            return objectSerializer.deserialize(value.getBytes(), Item.class).orElseThrow();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize item", e);
        }
    }
}
