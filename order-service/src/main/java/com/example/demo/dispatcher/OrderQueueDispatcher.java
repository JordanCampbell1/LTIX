package com.example.demo.dispatcher;

import com.example.demo.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderQueueDispatcher {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderQueueDispatcher.class);
    
    private final BlockingQueue<OrderEvent> orderQueue;
    private final ThreadPoolExecutor executor;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    
    // Configuration
    private static final int QUEUE_CAPACITY = 1000;
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 8;
    private static final long KEEP_ALIVE_TIME = 60L;
    
    public OrderQueueDispatcher() {
        // Create bounded queue
        this.orderQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        
        // Create thread pool with custom rejection policy
        this.executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new OrderThreadFactory(),
            new OrderRejectionPolicy()
        );
        
        // Start worker threads
        startWorkers();
        
        logger.info("OrderQueueDispatcher initialized with queue capacity: {}, core threads: {}, max threads: {}", 
                   QUEUE_CAPACITY, CORE_POOL_SIZE, MAX_POOL_SIZE);
    }
    
    /**
     * Enqueue an order event for processing
     * @throws RejectedExecutionException if queue is full
     */
    public boolean enqueue(OrderEvent orderEvent) {
        if (isShutdown.get()) {
            logger.warn("Attempted to enqueue order {} after shutdown", orderEvent.eventId());
            return false;
        }
        
        boolean accepted = orderQueue.offer(orderEvent);
        if (!accepted) {
            logger.error("Queue is full, rejecting order event: {}", orderEvent.eventId());
            throw new RejectedExecutionException("Order queue is full. Capacity: " + QUEUE_CAPACITY);
        }
        
        logger.debug("Order event {} enqueued successfully", orderEvent.eventId());
        return true;
    }
    
    /**
     * Get current queue size
     */
    public int getQueueSize() {
        return orderQueue.size();
    }
    
    /**
     * Get active thread count
     */
    public int getActiveThreadCount() {
        return executor.getActiveCount();
    }
    
    /**
     * Check if dispatcher is shutting down
     */
    public boolean isShutdown() {
        return isShutdown.get();
    }
    
    /**
     * Graceful shutdown
     */
    public void shutdown() {
        if (!isShutdown.compareAndSet(false, true)) {
            logger.info("Shutdown already in progress");
            return;
        }
        
        logger.info("Starting graceful shutdown of OrderQueueDispatcher");
        
        // Stop accepting new orders
        executor.shutdown();
        
        // Wait for existing tasks to complete
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate within 30 seconds, forcing shutdown");
                executor.shutdownNow();
                
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    logger.error("Executor did not terminate gracefully");
                }
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted during shutdown", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("OrderQueueDispatcher shutdown completed");
    }
    
    private void startWorkers() {
        for (int i = 0; i < CORE_POOL_SIZE; i++) {
            executor.submit(new OrderWorker());
        }
        logger.info("Started {} worker threads", CORE_POOL_SIZE);
    }
    
    private class OrderWorker implements Runnable {
        
        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            logger.debug("Worker thread {} started", threadName);
            
            try {
                while (!isShutdown.get() || !orderQueue.isEmpty()) {
                    try {
                        // Poll with timeout to allow shutdown detection
                        OrderEvent orderEvent = orderQueue.poll(1, TimeUnit.SECONDS);
                        
                        if (orderEvent != null) {
                            processOrder(orderEvent);
                        }
                    } catch (InterruptedException e) {
                        logger.info("Worker thread {} interrupted", threadName);
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } finally {
                logger.debug("Worker thread {} shutting down", threadName);
            }
        }
        
        private void processOrder(OrderEvent orderEvent) {
            try {
                logger.info("Processing order event: {} from source: {}", 
                           orderEvent.eventId(), orderEvent.source());
                
                // Simulate processing time
                Thread.sleep(100);
                
                // TODO: Publish to Kafka (for now just log)
                logger.info("Order event {} processed successfully. " +
                           "Symbol: {}, Side: {}, Quantity: {}, Price: {}", 
                           orderEvent.eventId(),
                           orderEvent.orderRequest().symbol(),
                           orderEvent.orderRequest().side(),
                           orderEvent.orderRequest().quantity(),
                           orderEvent.orderRequest().price());
                
            } catch (Exception e) {
                logger.error("Error processing order event: {}", orderEvent.eventId(), e);
                // In production, you might want to implement retry logic or dead letter queue
            }
        }
    }
    
    /**
     * Custom thread factory for naming threads
     */
    private static class OrderThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "order-worker-" + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    }
    
    /**
     * Custom rejection policy for when queue is full
     */
    private static class OrderRejectionPolicy implements RejectedExecutionHandler {
        
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            logger.error("Task rejected due to queue being full. " +
                        "Current pool size: {}, Queue size: {}", 
                        executor.getPoolSize(), executor.getQueue().size());
            
            // Throw exception to notify caller
            throw new RejectedExecutionException("Order processing queue is full");
        }
    }
}
