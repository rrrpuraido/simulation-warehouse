# Warehouse and Order Fulfillment System

A monolithic Java Spring Boot application that manages products, inventory, and orders for a warehouse and order fulfillment system.

## Overview

This application provides a comprehensive solution for managing a warehouse's inventory and order fulfillment processes. It allows for:

- Product management (add, edit, remove products)
- Customer management (register new customers, update customer information)
- Order fulfillment (create orders, track order status)
- Inventory management (track product quantities, handle low inventory alerts)
- Supplier management (manage supplier information for restocking)

## Technologies Used

- Java 21
- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- Spring Security
- Maven

## Domain Model

The application is built around the following core entities:

1. **Product** - Represents a saleable item in the warehouse
2. **Customer** - The end-customer placing orders
3. **Order** - A customer order that references one or more products
4. **OrderItem** - Represents a product in an order with quantity and price
5. **Inventory** - Tracks quantity on hand for each product
6. **Supplier** - The external entity that restocks products when inventory is low

## Features

### Product Management
- Add, edit, and remove products
- Each product has fields like id, name, description, sku, price, and thresholdQuantity

### Customer Management
- Register new customers
- Update customer information
- Store basic customer details like name, email, and shipping address

### Order Fulfillment
- Create new orders for one or more products
- Reduce inventory when an order is placed
- Track order status (CREATED, PAID, SHIPPED, COMPLETED)

### Inventory Management
- Track quantities for each product
- Prevent orders for products with insufficient inventory
- Generate alerts when inventory falls below threshold

### Supplier & Replenishment
- Manage supplier information
- Generate restock requests when inventory is low

### Event System
- OrderPlacedEvent - Published when an order transitions from CREATED to PAID
- LowInventoryEvent - Published when product inventory falls below thresholdQuantity

## API Endpoints

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/sku/{sku}` - Get product by SKU
- `POST /api/products` - Create a new product
- `PUT /api/products/{id}` - Update a product
- `DELETE /api/products/{id}` - Delete a product

### Customers
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers/email/{email}` - Get customer by email
- `POST /api/customers` - Create a new customer
- `PUT /api/customers/{id}` - Update a customer
- `DELETE /api/customers/{id}` - Delete a customer

### Orders
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/customer/{customerId}` - Get orders by customer
- `GET /api/orders/status/{status}` - Get orders by status
- `POST /api/orders/customer/{customerId}` - Create a new order
- `PUT /api/orders/{orderId}/status/{status}` - Update order status

### Inventory
- `GET /api/inventory` - Get all inventory
- `GET /api/inventory/{id}` - Get inventory by ID
- `GET /api/inventory/product/{productId}` - Get inventory by product ID
- `GET /api/inventory/low` - Get all low inventory items
- `PUT /api/inventory/product/{productId}/quantity/{quantity}` - Update inventory quantity
- `PUT /api/inventory/product/{productId}/add/{quantity}` - Add to inventory quantity
- `PUT /api/inventory/product/{productId}/reduce/{quantity}` - Reduce inventory quantity

### Suppliers
- `GET /api/suppliers` - Get all suppliers
- `GET /api/suppliers/{id}` - Get supplier by ID
- `GET /api/suppliers/email/{email}` - Get supplier by email
- `POST /api/suppliers` - Create a new supplier
- `PUT /api/suppliers/{id}` - Update a supplier
- `DELETE /api/suppliers/{id}` - Delete a supplier

## Running the Application

### Prerequisites
- Java 21

### Building and Running
```bash
# Clone the repository
git clone https://github.com/rrrpuraido/simulation-warehouse.git
cd simulation-warehouse

# Build the application
mvn clean package

# Run the application
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

### Authentication
The application uses basic authentication:
- Username: admin
- Password: admin

## Future Improvements

- Add more comprehensive test coverage
- Implement proper versioning for inventory to prevent concurrency issues
- Improve transaction management for events
- Enhance security with proper role-based access control
- Add a frontend interface
- Implement proper error handling for low inventory scenarios
- Separate concerns in the OrderService class
- Complete the RestockService implementation


## Issues
- Inventory always 0 when creating an order
- Creating order results in exception
- 
