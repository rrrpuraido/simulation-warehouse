package com.example.warehouse.event.listener;

import com.example.warehouse.event.OrderPlacedEvent;
import com.example.warehouse.event.OrderShippedEvent;
import com.example.warehouse.event.OrderCompletedEvent;
import com.example.warehouse.model.Order;
import com.example.warehouse.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepository orderRepository;

    @Async
    @EventListener
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        Long orderId = event.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        log.info("Order placed event received for order ID: {}, customer: {}",
                order.getId(), order.getCustomer().getName());

        // Additional business logic for order placed event
        // For example: notify analytics system, update reporting data, etc.
    }

    @Async
    @EventListener
    public void handleOrderShippedEvent(OrderShippedEvent event) {
        Long orderId = event.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        log.info("Order shipped event received for order ID: {}, customer: {}",
                order.getId(), order.getCustomer().getName());

        // Additional business logic for order shipped event
        // For example: update delivery tracking system, notify logistics partners, etc.
    }

    @Async
    @EventListener
    public void handleOrderCompletedEvent(OrderCompletedEvent event) {
        Long orderId = event.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        log.info("Order completed event received for order ID: {}, customer: {}",
                order.getId(), order.getCustomer().getName());

        // Additional business logic for order completed event
        // For example: update customer loyalty points, trigger customer satisfaction survey, etc.
    }
}
