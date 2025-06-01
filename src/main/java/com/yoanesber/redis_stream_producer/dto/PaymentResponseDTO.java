package com.yoanesber.redis_stream_producer.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for Payment Responses.
 * This class is used to encapsulate the data returned after processing a payment request.
 * It includes fields for the transaction ID, payment status, and creation time.
 */

@Data
@Getter
@Setter
@NoArgsConstructor // Required for Jackson deserialization when receiving JSON requests.
@AllArgsConstructor // Helps create DTO objects easily (useful when converting from entities).
public class PaymentResponseDTO {
    private String transactionId; // Reference from payment gateway
    private String paymentStatus; // PENDING, SUCCESS, FAILED
    private Instant createdAt = Instant.now(); // Payment creation time
    
    public PaymentResponseDTO(String transactionId, String paymentStatus) {
        this.transactionId = transactionId;
        this.paymentStatus = paymentStatus;
    }
}
