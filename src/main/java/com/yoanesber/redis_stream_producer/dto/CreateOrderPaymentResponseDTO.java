package com.yoanesber.redis_stream_producer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating an order payment response.
 * This class is used to encapsulate the response data when an order payment is created.
 * It includes details such as order ID, transaction ID, payment status, amount, currency, payment method, and creation time.
 * It is annotated with Lombok annotations to reduce boilerplate code for getters, setters, and constructors.
 */

@Data
@Getter
@Setter
@NoArgsConstructor // Required for Jackson deserialization when receiving JSON requests.
@AllArgsConstructor // Helps create DTO objects easily (useful when converting from entities).
public class CreateOrderPaymentResponseDTO {
    private String orderId; // Order identifier (linked to Orders table)
    private String transactionId; // Reference from payment gateway
    private String paymentStatus; // PENDING, SUCCESS, FAILED
    private BigDecimal amount; // Payment amount
    private String currency; // e.g., USD, EUR
    private String paymentMethod; // e.g., CREDIT_CARD, PAYPAL, BANK_TRANSFER
    private Instant createdAt; // Payment creation time
}
