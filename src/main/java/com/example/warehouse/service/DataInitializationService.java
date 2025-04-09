package com.example.warehouse.service;

import com.example.warehouse.model.*;
import com.example.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        // Check if data already exists
        if (customerRepository.count() > 0) {
            log.info("Data already initialized, skipping...");
            return;
        }

        log.info("Initializing test data...");

        // Create suppliers
        List<Supplier> suppliers = createSuppliers();
        
        // Create products
        List<Product> products = createProducts();
        
        // Initialize inventory
        initializeInventory(products);
        
        // Create customers
        List<Customer> customers = createCustomers();
        
        // Create orders (avg 5 per customer)
        createOrders(customers, products);

        log.info("Test data initialization completed successfully!");
    }

    private List<Supplier> createSuppliers() {
        log.info("Creating suppliers...");
        List<Supplier> suppliers = new ArrayList<>();
        
        suppliers.add(createSupplier("ABC Electronics", "contact@abcelectronics.com", "123-456-7890", "John Smith", "123 Main St, Electronics City, CA 94016"));
        suppliers.add(createSupplier("XYZ Distribution", "info@xyzdist.com", "987-654-3210", "Jane Doe", "456 Market St, Distribution Center, NY 10001"));
        suppliers.add(createSupplier("Global Supplies", "contact@globalsupplies.com", "555-123-4567", "Robert Johnson", "789 Supply Ave, Warehouse District, TX 75001"));
        suppliers.add(createSupplier("Tech Components", "sales@techcomponents.com", "444-555-6666", "Sarah Williams", "321 Tech Blvd, Component Park, WA 98001"));
        suppliers.add(createSupplier("Quality Parts Inc", "info@qualityparts.com", "777-888-9999", "Michael Brown", "654 Quality Rd, Parts Town, IL 60001"));
        
        return suppliers;
    }
    
    private Supplier createSupplier(String name, String email, String phone, String contactPerson, String address) {
        Supplier supplier = new Supplier();
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhone(phone);
        supplier.setContactPerson(contactPerson);
        supplier.setAddress(address);
        return supplierRepository.save(supplier);
    }

    private List<Product> createProducts() {
        log.info("Creating products...");
        List<Product> products = new ArrayList<>();
        
        products.add(createProduct("Laptop", "High-performance laptop with 16GB RAM and 512GB SSD", "TECH-001", new BigDecimal("999.99"), 5));
        products.add(createProduct("Smartphone", "Latest smartphone with 128GB storage", "TECH-002", new BigDecimal("699.99"), 10));
        products.add(createProduct("Tablet", "10-inch tablet with 64GB storage", "TECH-003", new BigDecimal("349.99"), 8));
        products.add(createProduct("Desktop Computer", "Powerful desktop with 32GB RAM and 1TB SSD", "TECH-004", new BigDecimal("1299.99"), 3));
        products.add(createProduct("Wireless Headphones", "Noise-cancelling wireless headphones", "AUDIO-001", new BigDecimal("199.99"), 15));
        products.add(createProduct("Bluetooth Speaker", "Portable Bluetooth speaker with 20-hour battery life", "AUDIO-002", new BigDecimal("89.99"), 12));
        products.add(createProduct("4K Monitor", "27-inch 4K monitor with HDR", "DISP-001", new BigDecimal("349.99"), 6));
        products.add(createProduct("Wireless Mouse", "Ergonomic wireless mouse", "ACC-001", new BigDecimal("29.99"), 20));
        products.add(createProduct("Mechanical Keyboard", "RGB mechanical keyboard with Cherry MX switches", "ACC-002", new BigDecimal("129.99"), 10));
        products.add(createProduct("External Hard Drive", "2TB external hard drive", "STORAGE-001", new BigDecimal("79.99"), 15));
        products.add(createProduct("USB Flash Drive", "128GB USB 3.0 flash drive", "STORAGE-002", new BigDecimal("24.99"), 25));
        products.add(createProduct("Wireless Charger", "10W Qi wireless charger", "POWER-001", new BigDecimal("34.99"), 18));
        products.add(createProduct("Power Bank", "20000mAh power bank with USB-C", "POWER-002", new BigDecimal("49.99"), 15));
        products.add(createProduct("HDMI Cable", "6ft HDMI 2.1 cable", "CABLE-001", new BigDecimal("14.99"), 30));
        products.add(createProduct("USB-C Cable", "3ft USB-C to USB-C cable", "CABLE-002", new BigDecimal("19.99"), 25));
        
        return products;
    }
    
    private Product createProduct(String name, String description, String sku, BigDecimal price, Integer thresholdQuantity) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setSku(sku);
        product.setPrice(price);
        product.setThresholdQuantity(thresholdQuantity);
        return productRepository.save(product);
    }
    
    private void initializeInventory(List<Product> products) {
        log.info("Initializing inventory...");
        
        for (Product product : products) {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            
            // Set random inventory quantity between threshold and threshold*4
            int minQuantity = product.getThresholdQuantity();
            int maxQuantity = minQuantity * 4;
            int quantity = ThreadLocalRandom.current().nextInt(minQuantity, maxQuantity + 1);
            
            // Intentionally set some products to low stock
            if (ThreadLocalRandom.current().nextDouble() < 0.2) { // 20% chance
                quantity = ThreadLocalRandom.current().nextInt(0, minQuantity + 1);
            }
            
            inventory.setQuantity(quantity);
            inventoryRepository.save(inventory);
        }
    }

    private List<Customer> createCustomers() {
        log.info("Creating customers...");
        List<Customer> customers = new ArrayList<>();
        
        customers.add(createCustomer("John Smith", "john.smith@example.com", "123 Main St, Apt 4B, New York, NY 10001"));
        customers.add(createCustomer("Jane Doe", "jane.doe@example.com", "456 Park Ave, San Francisco, CA 94016"));
        customers.add(createCustomer("Robert Johnson", "robert.johnson@example.com", "789 Oak St, Chicago, IL 60601"));
        customers.add(createCustomer("Sarah Williams", "sarah.williams@example.com", "321 Pine St, Seattle, WA 98101"));
        customers.add(createCustomer("Michael Brown", "michael.brown@example.com", "654 Maple Ave, Boston, MA 02108"));
        customers.add(createCustomer("Emily Davis", "emily.davis@example.com", "987 Cedar Rd, Austin, TX 78701"));
        customers.add(createCustomer("David Miller", "david.miller@example.com", "159 Birch Ln, Denver, CO 80202"));
        customers.add(createCustomer("Jennifer Wilson", "jennifer.wilson@example.com", "753 Spruce Dr, Portland, OR 97201"));
        customers.add(createCustomer("James Taylor", "james.taylor@example.com", "852 Redwood Blvd, Miami, FL 33101"));
        customers.add(createCustomer("Lisa Anderson", "lisa.anderson@example.com", "426 Elm St, Atlanta, GA 30301"));
        
        return customers;
    }
    
    private Customer createCustomer(String name, String email, String shippingAddress) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setShippingAddress(shippingAddress);
        return customerRepository.save(customer);
    }
    
    private void createOrders(List<Customer> customers, List<Product> products) {
        log.info("Creating orders...");
        
        // Create an average of 5 orders per customer
        for (Customer customer : customers) {
            // Random number of orders between 3 and 7
            int numOrders = ThreadLocalRandom.current().nextInt(3, 8);
            
            for (int i = 0; i < numOrders; i++) {
                createOrder(customer, products);
            }
        }
    }
    
    private void createOrder(Customer customer, List<Product> products) {
        Order order = new Order();
        order.setCustomer(customer);
        
        // Set order date to a random time in the past 30 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime orderDate = now.minusDays(ThreadLocalRandom.current().nextInt(0, 31))
                                    .minusHours(ThreadLocalRandom.current().nextInt(0, 24))
                                    .minusMinutes(ThreadLocalRandom.current().nextInt(0, 60));
        order.setOrderDate(orderDate);
        
        // Set random order status
        OrderStatus[] statuses = OrderStatus.values();
        OrderStatus status = statuses[ThreadLocalRandom.current().nextInt(0, statuses.length)];
        order.setStatus(status);
        
        order.setShippingAddress(customer.getShippingAddress());
        
        // Add random number of items to the order (1-5)
        int numItems = ThreadLocalRandom.current().nextInt(1, 6);
        
        // Shuffle products to get random ones
        List<Product> shuffledProducts = new ArrayList<>(products);
        Collections.shuffle(shuffledProducts);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Add items to the order
        for (int i = 0; i < numItems && i < shuffledProducts.size(); i++) {
            Product product = shuffledProducts.get(i);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            
            // Random quantity between 1 and 3
            int quantity = ThreadLocalRandom.current().nextInt(1, 4);
            orderItem.setQuantity(quantity);
            
            orderItem.setUnitPrice(product.getPrice());
            orderItem.calculateSubtotal();
            
            order.addItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }
        
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }
}