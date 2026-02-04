package com.example.demo.controller;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.entity.OrderEntity;
import com.example.demo.repository.OrderRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/order")
@Validated
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setSymbol(request.symbol());
        order.setSide(request.side());
        order.setQuantity(request.quantity());
        order.setPrice(request.price());
        
        OrderEntity savedOrder = orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(savedOrder));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(OrderResponse.from(order)))
                .orElse(ResponseEntity.notFound().build());
    }
}
