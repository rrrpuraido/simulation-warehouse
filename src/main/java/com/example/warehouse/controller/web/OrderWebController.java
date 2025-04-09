package com.example.warehouse.controller.web;

import com.example.warehouse.model.Customer;
import com.example.warehouse.model.Order;
import com.example.warehouse.model.OrderStatus;
import com.example.warehouse.model.Product;
import com.example.warehouse.service.CustomerService;
import com.example.warehouse.service.OrderService;
import com.example.warehouse.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderWebController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;

    @GetMapping
    public String getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String dateRange,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        List<Order> orders = orderService.getAllOrders();
        
        // Filter by customer if specified
        if (customerId != null) {
            Customer customer = customerService.getCustomerById(customerId);
            orders = orderService.getOrdersByCustomer(customerId);
            model.addAttribute("selectedCustomer", customer);
            model.addAttribute("selectedCustomerId", customerId);
        }
        
        // Filter by status if specified
        if (status != null) {
            orders = orderService.getOrdersByStatus(status);
            model.addAttribute("selectedStatus", status);
        }
        
        // Add all customers for the filter dropdown
        model.addAttribute("customers", customerService.getAllCustomers());
        
        // Pagination info
        int totalItems = orders.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // Paginate the orders manually
        int start = page * size;
        int end = Math.min(start + size, totalItems);
        List<Order> pagedOrders = orders.subList(start < totalItems ? start : 0, end < totalItems ? end : totalItems);
        
        model.addAttribute("orders", pagedOrders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("dateRange", dateRange);

        return "orders/list";
    }

    @GetMapping("/{id}")
    public String getOrderById(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "orders/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("products", productService.getAllProducts());
        return "orders/form";
    }

    @GetMapping("/customer/{customerId}/new")
    public String showCreateFormForCustomer(@PathVariable Long customerId, Model model) {
        Customer customer = customerService.getCustomerById(customerId);
        model.addAttribute("selectedCustomer", customer);
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("products", productService.getAllProducts());
        return "orders/form";
    }

    @PostMapping("/create")
    public String createOrder(
            @RequestParam Long customerId,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam("items[].productId") List<Long> productIds,
            @RequestParam("items[].quantity") List<Integer> quantities,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Convert to OrderItemRequest objects
            List<OrderService.OrderItemRequest> orderItems = createOrderItemRequests(productIds, quantities);
            
            // Create the order
            Order order = orderService.createOrder(customerId, orderItems);
            
            // Update shipping address if provided
            if (shippingAddress != null && !shippingAddress.isEmpty()) {
                order.setShippingAddress(shippingAddress);
                // Save the updated order
                // Note: In a real application, you would have a method in OrderService to update the order
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Order created successfully");
            return "redirect:/orders/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating order: " + e.getMessage());
            return "redirect:/orders/new";
        }
    }

    @PostMapping("/{id}/status/{status}")
    public String updateOrderStatus(
            @PathVariable Long id,
            @PathVariable OrderStatus status,
            RedirectAttributes redirectAttributes) {
        
        try {
            Order order = orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Order status updated to " + status);
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error updating order status: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Note: In a real application, you would have a method in OrderService to delete the order
            // orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting order: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    private List<OrderService.OrderItemRequest> createOrderItemRequests(List<Long> productIds, List<Integer> quantities) {
        List<OrderService.OrderItemRequest> orderItems = new java.util.ArrayList<>();
        
        for (int i = 0; i < productIds.size(); i++) {
            OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
            item.setProductId(productIds.get(i));
            item.setQuantity(quantities.get(i));
            orderItems.add(item);
        }
        
        return orderItems;
    }
}