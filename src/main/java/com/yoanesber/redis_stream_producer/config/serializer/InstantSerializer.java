package com.yoanesber.redis_stream_producer.config.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Custom serializer for Instant objects to format them as ISO-8601 strings.
 * This is useful for serializing Instant fields in DTOs or entities to JSON.
 */

public class InstantSerializer extends JsonSerializer<Instant> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    // Serialize the Instant object to a string
    @Override
    public void serialize(Instant instant, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(FORMATTER.format(instant));
    }
}
