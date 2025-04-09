package com.example.warehouse.service;

import com.example.warehouse.event.OrderPlacedEvent;
import com.example.warehouse.event.OrderShippedEvent;
import com.example.warehouse.event.OrderCompletedEvent;
import com.example.warehouse.model.*;
import com.example.warehouse.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        return orderRepository.findByCustomer(customer);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order createOrder(Long customerId, List<OrderItemRequest> orderItems) {
        Customer customer = customerService.getCustomerById(customerId);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);
        order.setShippingAddress(customer.getShippingAddress());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : orderItems) {
            Product product = productService.getProductById(itemRequest.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.calculateSubtotal();

            order.addItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);

        // If order is transitioning from CREATED to PAID, reduce inventory and publish event
        if (oldStatus == OrderStatus.CREATED && newStatus == OrderStatus.PAID) {
            processOrderPayment(savedOrder);
        }

        // If order is transitioning to SHIPPED, handle shipping logic
        if (newStatus == OrderStatus.SHIPPED) {
            handleOrderShipping(savedOrder);
        }

        // If order is transitioning to COMPLETED, handle completion logic
        if (newStatus == OrderStatus.COMPLETED) {
            handleOrderCompletion(savedOrder);
        }

        return savedOrder;
    }

    private void processOrderPayment(Order order) {
        log.info("Processing payment for order: {}", order.getId());

        // Reduce inventory for each item in the order
        for (OrderItem item : order.getItems()) {
            inventoryService.reduceInventoryQuantity(item.getProduct().getId(), item.getQuantity());
        }

        eventPublisher.publishEvent(new OrderPlacedEvent(this, order.getId()));

        // Send email notification
        sendOrderConfirmationEmail(order);
    }

    private void handleOrderShipping(Order order) {
        log.info("Preparing order for shipping: {}", order.getId());

        // Shipping logic
        prepareShippingLabel(order);
        notifyShippingDepartment(order);

        // Publish order shipped event
        eventPublisher.publishEvent(new OrderShippedEvent(this, order.getId()));

        sendShippingConfirmationEmail(order);
    }

    private void handleOrderCompletion(Order order) {
        log.info("Completing order: {}", order.getId());

        // Completion logic
        updateCustomerPurchaseHistory(order);

        // Publish order completed event
        eventPublisher.publishEvent(new OrderCompletedEvent(this, order.getId()));

        sendOrderCompletionEmail(order);
    }

    private void sendOrderConfirmationEmail(Order order) {
        log.info("Sending order confirmation email to customer: {}", order.getCustomer().getEmail());
        // In a real application, this would send an actual email
    }

    private void sendShippingConfirmationEmail(Order order) {
        log.info("Sending shipping confirmation email to customer: {}", order.getCustomer().getEmail());
        // In a real application, this would send an actual email
    }

    private void sendOrderCompletionEmail(Order order) {
        log.info("Sending order completion email to customer: {}", order.getCustomer().getEmail());
        // In a real application, this would send an actual email
    }

    private void prepareShippingLabel(Order order) {
        log.info("Preparing shipping label for order: {}", order.getId());
        // In a real application, this would generate a shipping label
    }

    private void notifyShippingDepartment(Order order) {
        log.info("Notifying shipping department about order: {}", order.getId());
        // In a real application, this would notify the shipping department
    }

    private void updateCustomerPurchaseHistory(Order order) {
        log.info("Updating purchase history for customer: {}", order.getCustomer().getId());
        // In a real application, this would update the customer's purchase history
    }

    // Helper class for order item requests
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
