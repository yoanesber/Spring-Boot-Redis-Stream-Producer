package com.yoanesber.redis_stream_producer.service;

import com.yoanesber.redis_stream_producer.dto.CreateOrderPaymentRequestDTO;
import com.yoanesber.redis_stream_producer.entity.OrderPayment;

public interface OrderPaymentService {
    // Create a new OrderPayment record.
    OrderPayment createOrderPayment(CreateOrderPaymentRequestDTO orderPaymentDTO);
}
