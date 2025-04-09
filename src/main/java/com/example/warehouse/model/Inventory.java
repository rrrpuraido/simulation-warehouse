package com.example.warehouse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    // Intentionally omitting @Version annotation to create concurrency issues
    // This will lead to race conditions when multiple orders update the same inventory
    
    public void reduceQuantity(int amount) {
        // Intentional bug: No proper check for negative inventory
        // This will allow inventory to go negative in race conditions
        this.quantity = this.quantity - amount;
        
        // If quantity becomes negative, just set it to zero
        // This is an incomplete error handling that doesn't properly fail the operation
        if (this.quantity < 0) {
            this.quantity = 0;
        }
    }
}