package com.example.demo;

import com.example.demo.dispatcher.OrderQueueDispatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class LtixApplication {

    private static ConfigurableApplicationContext context;
    private static OrderQueueDispatcher orderQueueDispatcher;

    public static void main(String[] args) {
        context = SpringApplication.run(LtixApplication.class, args);
        
        // Get the dispatcher bean for shutdown handling
        orderQueueDispatcher = context.getBean(OrderQueueDispatcher.class);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered - gracefully shutting down OrderQueueDispatcher");
            if (orderQueueDispatcher != null) {
                orderQueueDispatcher.shutdown();
            }
        }));
    }

    @PreDestroy
    public void onShutdown() {
        if (orderQueueDispatcher != null) {
            orderQueueDispatcher.shutdown();
        }
    }
}
