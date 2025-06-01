package com.yoanesber.redis_stream_producer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoanesber.redis_stream_producer.dto.CreateOrderPaymentRequestDTO;
import com.yoanesber.redis_stream_producer.dto.CreateOrderPaymentResponseDTO;
import com.yoanesber.redis_stream_producer.entity.CustomHttpResponse;
import com.yoanesber.redis_stream_producer.entity.OrderPayment;
import com.yoanesber.redis_stream_producer.service.OrderPaymentService;

/**
 * OrderPaymentController handles HTTP requests related to order payments.
 * It provides an endpoint to create a new order payment record.
 */

@RestController
@RequestMapping("/api/v1/order-payment")
public class OrderPaymentController {
    private final OrderPaymentService orderPaymentService;

    public OrderPaymentController(OrderPaymentService orderPaymentService) {
        this.orderPaymentService = orderPaymentService;
    }

    @PostMapping
    public ResponseEntity<CustomHttpResponse> createOrderPayment(@RequestBody CreateOrderPaymentRequestDTO orderPaymentDTO) {
        try {
            // Create a new OrderPayment record using the service layer.
            OrderPayment orderPayment = orderPaymentService.createOrderPayment(orderPaymentDTO);

            // Check if the order payment was created successfully.
            if (orderPayment == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomHttpResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                        "Failed to create order payment", 
                        null));
            }

            // Return a successful response with the created order payment details.
            // The response includes the order ID, transaction ID, payment status, amount, currency, payment method, and creation time.
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CustomHttpResponse(HttpStatus.CREATED.value(),
                "Order payment created successfully", 
                new CreateOrderPaymentResponseDTO(orderPayment.getOrderId(), 
                    orderPayment.getTransactionId(),
                    orderPayment.getPaymentStatus(), 
                    orderPayment.getAmount(),
                    orderPayment.getCurrency(),
                    orderPayment.getPaymentMethod(),
                    orderPayment.getCreatedAt())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomHttpResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    e.getMessage(), 
                    null));
        }
    }
}
