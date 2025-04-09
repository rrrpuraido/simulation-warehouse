package com.example.warehouse.service;

import com.example.warehouse.event.OrderPlacedEvent;
import com.example.warehouse.model.*;
import com.example.warehouse.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private ProductService productService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private Customer customer;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setEmail("test@example.com");
        customer.setShippingAddress("123 Test St");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setSku("SKU001");
        product.setPrice(new BigDecimal("10.00"));
        product.setThresholdQuantity(5);

        order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setShippingAddress(customer.getShippingAddress());
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenOrderExists() {
        // Arrange
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(order, result);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() {
        // Arrange
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.PAID;
        
        Order updatedOrder = new Order();
        updatedOrder.setId(orderId);
        updatedOrder.setCustomer(customer);
        updatedOrder.setOrderDate(order.getOrderDate());
        updatedOrder.setStatus(newStatus);
        updatedOrder.setTotalAmount(order.getTotalAmount());
        updatedOrder.setShippingAddress(order.getShippingAddress());
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        
        // Act
        Order result = orderService.updateOrderStatus(orderId, newStatus);
        
        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WhenTransitioningToPaid_ShouldPublishEvent() {
        // Arrange
        Long orderId = 1L;
        OrderStatus oldStatus = OrderStatus.CREATED;
        OrderStatus newStatus = OrderStatus.PAID;
        
        Order initialOrder = new Order();
        initialOrder.setId(orderId);
        initialOrder.setCustomer(customer);
        initialOrder.setOrderDate(LocalDateTime.now());
        initialOrder.setStatus(oldStatus);
        initialOrder.setTotalAmount(new BigDecimal("10.00"));
        initialOrder.setShippingAddress(customer.getShippingAddress());
        
        // Add an order item
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(product.getPrice());
        orderItem.setSubtotal(product.getPrice());
        initialOrder.addItem(orderItem);
        
        Order savedOrder = new Order();
        savedOrder.setId(orderId);
        savedOrder.setCustomer(customer);
        savedOrder.setOrderDate(initialOrder.getOrderDate());
        savedOrder.setStatus(newStatus);
        savedOrder.setTotalAmount(initialOrder.getTotalAmount());
        savedOrder.setShippingAddress(initialOrder.getShippingAddress());
        savedOrder.addItem(orderItem);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(initialOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        // Act
        Order result = orderService.updateOrderStatus(orderId, newStatus);
        
        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        
        // Verify that inventory was reduced
        verify(inventoryService, times(1)).reduceInventoryQuantity(product.getId(), orderItem.getQuantity());
        
        // Verify that event was published
        // This demonstrates the event transaction boundary issue:
        // The event is published before the transaction is committed
        verify(eventPublisher, times(1)).publishEvent(any(OrderPlacedEvent.class));
        
        // In a real scenario, if the transaction fails after this point,
        // the event would still have been processed, leading to inconsistent data
    }

    // This test is commented out because it would demonstrate the event transaction boundary issue
    /*
    @Test
    void updateOrderStatus_WhenTransactionFails_EventStillPublished() {
        // Arrange
        Long orderId = 1L;
        OrderStatus oldStatus = OrderStatus.CREATED;
        OrderStatus newStatus = OrderStatus.PAID;
        
        Order initialOrder = new Order();
        initialOrder.setId(orderId);
        initialOrder.setCustomer(customer);
        initialOrder.setOrderDate(LocalDateTime.now());
        initialOrder.setStatus(oldStatus);
        initialOrder.setTotalAmount(new BigDecimal("10.00"));
        initialOrder.setShippingAddress(customer.getShippingAddress());
        
        // Add an order item
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(product.getPrice());
        orderItem.setSubtotal(product.getPrice());
        initialOrder.addItem(orderItem);
        
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(initialOrder));
        
        // Simulate transaction failure after event is published
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        try {
            orderService.updateOrderStatus(orderId, newStatus);
        } catch (RuntimeException e) {
            // Expected exception
        }
        
        // Verify that event was published even though transaction failed
        verify(eventPublisher, times(1)).publishEvent(any(OrderPlacedEvent.class));
        
        // This demonstrates the event transaction boundary issue:
        // The event is published before the transaction is committed,
        // so if the transaction fails, the event is still processed
    }
    */
}