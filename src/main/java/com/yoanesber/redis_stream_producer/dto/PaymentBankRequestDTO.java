package com.yoanesber.redis_stream_producer.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for Bank Payment Requests.
 * This class is used to encapsulate the data required for processing a bank payment request.
 * It includes fields for the order ID, payment amount, currency, bank account number, and bank name.
 */

@Data
@Getter
@Setter
@NoArgsConstructor // Required for Jackson deserialization when receiving JSON requests.
@AllArgsConstructor // Helps create DTO objects easily (useful when converting from entities).
public class PaymentBankRequestDTO {
    private String orderId; // Order identifier (linked to Orders table)
    private BigDecimal amount; // Payment amount
    private String currency; // e.g., USD, EUR
    private String bankAccount; // Bank account number
    private String bankName; // Bank name
}
