package com.yoanesber.redis_stream_producer.redis;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.yoanesber.redis_stream_producer.mapper.Converter;

/**
 * MessageProducer is a component that handles the production of messages to a Redis stream.
 * It generates unique IDs for each message based on the current timestamp and a sequence number,
 * ensuring that even if multiple messages are produced in the same millisecond, they will have unique IDs.
 * 
 * The component uses a RedisTemplate to interact with the Redis stream and provides methods to publish messages.
 * The messages are stored in a Redis stream with a maximum length, and older messages are trimmed when the limit is reached.
 */

@Component
public class MessageProducer {
    // Default maxlen and approximate trimming for the stream
    // maxlen: The maximum number of messages to keep in the stream. When the stream reaches this limit, older messages are removed.
    // approximateTrimming: If true, Redis will use an approximate algorithm to trim the stream. This is faster but may not guarantee that the stream size is exactly maxlen.
    // If false, Redis will use an exact algorithm, which is slower but guarantees that the stream size is exactly maxlen.
    private Long maxlen = 3L;
    private boolean approximateTrimming = true;

    // AtomicLong to keep track of the last timestamp and sequence number
    // lastTimestamp: The last timestamp used for generating IDs. This is used to ensure that IDs are unique even if multiple messages are generated in the same millisecond.
    // sequence: A counter that is incremented each time a message is generated in the same millisecond. This ensures that even if multiple messages are generated in the same millisecond, they will have unique IDs.
    private final AtomicLong lastTimestamp = new AtomicLong(0);
    private final AtomicLong sequence = new AtomicLong(0);

    private final RedisTemplate<String, Object> redisTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MessageProducer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Generates a unique ID for the message using the current timestamp and a sequence number.
     * The ID format is "timestamp-sequence", where timestamp is in milliseconds and sequence is a counter.
     * This ensures that even if multiple messages are sent in the same millisecond, they will have unique IDs.
     *
     * @return A unique RecordId for the message.
     */
    private RecordId generateID() {
        long currentTimestamp = System.currentTimeMillis();
        long last = lastTimestamp.get();

        if (currentTimestamp == last) {
            // if still in the same millisecond, increment the sequence
            long seq = sequence.incrementAndGet();
            return RecordId.of(currentTimestamp + "-" + seq);
        } else if (currentTimestamp > last) {
            // when the time has changed, reset the sequence to 0 and update the lastTimestamp
            // when multiple threads operate in the same millisecond
            if (lastTimestamp.compareAndSet(last, currentTimestamp)) {
                sequence.set(0);
                return RecordId.of(currentTimestamp + "-0");
            } else {
                // if race condition occurs, call again until safe
                // (this is a rare case, but we handle it for safety)
                return generateID();
            }
        } else {
            // when currentTimestamp < last
            // very rare case: time goes backward (e.g., due to NTP sync or system clock change)
            // in this case, we just increment the sequence to avoid collision
            // this is a fallback mechanism to ensure we always return a valid RecordId
            long fallbackSeq = sequence.incrementAndGet();
            return RecordId.of(last + "-" + fallbackSeq);
        }
    }

    /**
     * Publishes a message to the specified Redis stream.
     *
     * @param streamName The name of the Redis stream to publish the message to.
     * @param payload    The payload of the message to be published.
     */
    public void produce(String streamName, Object payload) {
        Assert.hasText(streamName, "Stream name must not be empty");
        Assert.notNull(payload, "Payload must not be null");

        // Check if the payload is a valid object
        if (payload instanceof String) {
            logger.error("Payload must be a valid object, not a string: {}", payload);
            throw new IllegalArgumentException("Payload must be a valid object, not a string: " + payload);
        }

        try {
            // Generate a unique ID for the message
            RecordId generateID = generateID();

            // Creating a map from the payload object
            Map<String, Object> messageMap = Converter.toMap(payload);
            if (messageMap == null) {
                logger.error("Failed to convert payload to map: {}", payload);
                throw new RuntimeException("Failed to convert payload to map: " + payload);
            }
            
            // Adding the generated ID to the message map
            // This ID is used to uniquely identify the message in the stream
            messageMap.put("id", generateID.getValue());
            
            // Adding the message to the stream
            RecordId recordId = redisTemplate.opsForStream().add(
                ObjectRecord.create(streamName, messageMap)
                    .withId(generateID), // using the generated ID
                XAddOptions
                    .maxlen(maxlen) // keeps at most `n` messages in the stream, removing older ones when the limit is reached.
                    .approximateTrimming(approximateTrimming) // set to `true` to use approximate trimming, for best performance when you don’t need an exact limit
            );

            // Check if the message was added successfully
            if (recordId == null) {
                logger.error("Failed to publish message to stream: {}", streamName);
                throw new RuntimeException("Failed to publish message to stream: " + streamName);
            } 

            logger.info("Published message to stream: {} with ID: {}", streamName, recordId);
        } catch (Exception e) {
            logger.error("Error publishing message to stream: {}", streamName, e);
            throw new RuntimeException("Error publishing message to stream: " + streamName, e);
        }
    }
}
