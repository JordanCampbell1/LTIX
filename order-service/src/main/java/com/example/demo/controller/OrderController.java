package com.example.demo.controller;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dispatcher.OrderQueueDispatcher;
import com.example.demo.entity.OrderEntity;
import com.example.demo.event.OrderEvent;
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
    private final OrderQueueDispatcher orderQueueDispatcher;

    public OrderController(OrderRepository orderRepository, OrderQueueDispatcher orderQueueDispatcher) {
        this.orderRepository = orderRepository;
        this.orderQueueDispatcher = orderQueueDispatcher;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // Create order entity for database storage
        OrderEntity order = new OrderEntity();
        order.setSymbol(request.symbol());
        order.setSide(request.side());
        order.setQuantity(request.quantity());
        order.setPrice(request.price());
        
        // Save to database
        OrderEntity savedOrder = orderRepository.save(order);
        
        // Enqueue order event for async processing
        OrderEvent orderEvent = new OrderEvent(request, "rest-api");
        try {
            orderQueueDispatcher.enqueue(orderEvent);
        } catch (Exception e) {
            // Log but don't fail the request - order is already saved
            System.err.println("Failed to enqueue order for processing: " + e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(savedOrder));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(OrderResponse.from(order)))
                .orElse(ResponseEntity.notFound().build());
    }
}
