package com.yoanesber.redis_stream_producer.service;

import com.yoanesber.redis_stream_producer.dto.CreateOrderPaymentRequestDTO;
import com.yoanesber.redis_stream_producer.entity.OrderPayment;

/**
 * OrderPaymentService interface defines the contract for services related to order payments.
 * It provides methods to create and manage order payment records.
 * This service is typically used by controllers to handle payment-related operations.
 */

public interface OrderPaymentService {
    // Create a new OrderPayment record.
    OrderPayment createOrderPayment(CreateOrderPaymentRequestDTO orderPaymentDTO);
}
