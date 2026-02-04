package com.example.demo.dto;

import com.example.demo.utils.OrderSide;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String symbol,
    OrderSide side,
    Long quantity,
    Double price,
    Instant createdAt
) {
    public static OrderResponse from(com.example.demo.entity.OrderEntity entity) {
        return new OrderResponse(
            entity.getId(),
            entity.getSymbol(),
            entity.getSide(),
            entity.getQuantity(),
            entity.getPrice(),
            entity.getCreatedAt()
        );
    }
}
