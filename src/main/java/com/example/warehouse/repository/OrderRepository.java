package com.example.warehouse.repository;

import com.example.warehouse.model.Customer;
import com.example.warehouse.model.Order;
import com.example.warehouse.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomer(Customer customer);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);
    
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
}