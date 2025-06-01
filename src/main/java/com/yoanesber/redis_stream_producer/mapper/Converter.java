package com.yoanesber.redis_stream_producer.mapper;

import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converter class for converting between Java objects and Maps.
 * This class provides methods to convert an entity to a Map and vice versa.
 * It uses Jackson's ObjectMapper for serialization and deserialization.
 *
 * This class is useful for scenarios where you need to convert complex objects
 * to a Map representation, such as when interacting with Redis or other data stores
 * that require a key-value format.
 */

public class Converter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static Map<String, Object> toMap(Object entity) throws IllegalArgumentException {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        
        if (entity instanceof String) {
            throw new IllegalArgumentException("Entity cannot be a String");
        }
        if (entity instanceof Number) {
            throw new IllegalArgumentException("Entity cannot be a Number");
        }
        if (entity instanceof Boolean) {
            throw new IllegalArgumentException("Entity cannot be a Boolean");
        }
        
        try {
            return objectMapper.convertValue(entity, new TypeReference<Map<String, Object>>() {});
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to convert entity to Map", e);
        }
    }

    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) throws IllegalArgumentException {
        if (map == null) {
            throw new IllegalArgumentException("Map cannot be null");
        }
        
        if (clazz == null) {
            throw new IllegalArgumentException("Class type cannot be null");
        }
        
        try {
            return objectMapper.convertValue(map, clazz);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to convert Map to entity", e);
        }
    }
}
