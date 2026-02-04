package com.example.demo.event;

import com.example.demo.dto.CreateOrderRequest;
import java.time.Instant;

public record OrderEvent(
    String eventId,
    CreateOrderRequest orderRequest,
    Instant timestamp,
    String source
) {
    public OrderEvent(CreateOrderRequest orderRequest, String source) {
        this(
            java.util.UUID.randomUUID().toString(),
            orderRequest,
            Instant.now(),
            source
        );
    }
}
