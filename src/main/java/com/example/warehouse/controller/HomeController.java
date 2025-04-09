package com.example.warehouse.controller;

import com.example.warehouse.model.Customer;
import com.example.warehouse.model.Inventory;
import com.example.warehouse.model.Order;
import com.example.warehouse.model.Product;
import com.example.warehouse.service.CustomerService;
import com.example.warehouse.service.InventoryService;
import com.example.warehouse.service.OrderService;
import com.example.warehouse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;
    private final InventoryService inventoryService;

    @GetMapping("/")
    public String home(Model model) {
        // Get metrics for dashboard
        Map<String, Object> metrics = new HashMap<>();

        // Order metrics
        List<Order> allOrders = orderService.getAllOrders();
        metrics.put("totalOrders", allOrders.size());

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long ordersToday = allOrders.stream()
                .filter(order -> order.getOrderDate().isAfter(today))
                .count();
        metrics.put("ordersToday", ordersToday);

        // Revenue metrics
        BigDecimal totalRevenue = allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        metrics.put("totalRevenue", totalRevenue);

        BigDecimal revenueToday = allOrders.stream()
                .filter(order -> order.getOrderDate().isAfter(today))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        metrics.put("revenueToday", revenueToday);

        // Customer metrics
        List<Customer> allCustomers = customerService.getAllCustomers();
        metrics.put("totalCustomers", allCustomers.size());

        // This is a placeholder - in a real app, you'd track customer creation date
        metrics.put("newCustomersToday", 0);

        // Inventory metrics
        List<Inventory> lowStockItems = inventoryService.getLowInventory();
        metrics.put("lowStockCount", lowStockItems.size());

        model.addAttribute("metrics", metrics);

        // Recent orders
        List<Order> recentOrders = allOrders.stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentOrders", recentOrders);

        // Low stock items
        model.addAttribute("lowStockItems", lowStockItems.stream().limit(5).collect(Collectors.toList()));

        // Top customers (placeholder - in a real app, you'd calculate this from order data)
        List<Map<String, Object>> topCustomersWithStats = allCustomers.stream()
                .limit(5)
                .map(customer -> {
                    Map<String, Object> customerMap = new HashMap<>();
                    customerMap.put("id", customer.getId());
                    customerMap.put("name", customer.getName());
                    customerMap.put("email", customer.getEmail());
                    customerMap.put("shippingAddress", customer.getShippingAddress());
                    // Add placeholder stats
                    customerMap.put("orderCount", 0);
                    customerMap.put("totalSpent", BigDecimal.ZERO);
                    return customerMap;
                })
                .collect(Collectors.toList());

        model.addAttribute("topCustomers", topCustomersWithStats);

        // Top products (placeholder - in a real app, you'd calculate this from order data)
        List<Product> allProducts = productService.getAllProducts();

        // Create a list of product DTOs with additional properties
        List<Map<String, Object>> topProductsWithStats = allProducts.stream()
                .limit(5)
                .map(product -> {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", product.getId());
                    productMap.put("name", product.getName());
                    productMap.put("sku", product.getSku());
                    productMap.put("price", product.getPrice());
                    productMap.put("description", product.getDescription());
                    productMap.put("thresholdQuantity", product.getThresholdQuantity());
                    // Add placeholder stats
                    productMap.put("unitsSold", 0);
                    productMap.put("revenue", BigDecimal.ZERO);
                    return productMap;
                })
                .collect(Collectors.toList());

        model.addAttribute("topProducts", topProductsWithStats);

        return "index";
    }
}
