package com.yoanesber.redis_stream_producer.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for Credit Card Payment Requests.
 * This class is used to encapsulate the data required for processing a credit card payment request.
 * It includes fields for the order ID, payment amount, currency, credit card number,
 * credit card expiry date, and credit card CVV (Card Verification Value).
 */

@Data
@Getter
@Setter
@NoArgsConstructor // Required for Jackson deserialization when receiving JSON requests.
@AllArgsConstructor // Helps create DTO objects easily (useful when converting from entities).
public class PaymentCCRequestDTO {
    private String orderId; // Order identifier (linked to Orders table)
    private BigDecimal amount; // Payment amount
    private String currency; // e.g., USD, EUR
    private String cardNumber; // Credit card number
    private String cardExpiry; // Credit card expiry date
    private String cardCvv; // Credit card CVV (Card Verification Value)
}
