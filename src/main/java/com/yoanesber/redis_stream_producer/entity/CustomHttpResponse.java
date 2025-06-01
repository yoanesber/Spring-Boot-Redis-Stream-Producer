package com.yoanesber.redis_stream_producer.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Custom HTTP Response class to standardize API responses.
 * This class encapsulates the status code, timestamp, message, and any additional data.
 * It is used to provide a consistent response structure across the application.
 */

@Data
@Getter
@Setter
@NoArgsConstructor // Required for Jackson deserialization when receiving JSON requests.
@AllArgsConstructor // Helps create DTO objects easily (useful when converting from entities).
public class CustomHttpResponse {
    private Integer statusCode;
    private Instant timestamp = Instant.now();
    private String message;
    private Object data;

    public CustomHttpResponse(Integer statusCode, String message, Object data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }
}
