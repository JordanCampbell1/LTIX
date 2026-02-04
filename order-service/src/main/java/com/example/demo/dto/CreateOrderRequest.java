package com.example.demo.dto;

import com.example.demo.utils.OrderSide;
import jakarta.validation.constraints.*;

public record CreateOrderRequest(
    
    @NotBlank(message = "Symbol is required")
    @Size(min = 1, max = 20, message = "Symbol must be between 1 and 20 characters")
    String symbol,
    
    @NotNull(message = "Side is required")
    OrderSide side,
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Long quantity,
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    Double price
) {}
