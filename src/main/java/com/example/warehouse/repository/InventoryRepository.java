package com.example.warehouse.repository;

import com.example.warehouse.model.Inventory;
import com.example.warehouse.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    Optional<Inventory> findByProduct(Product product);
    
    Optional<Inventory> findByProductId(Long productId);
    
    // Query to find products with inventory below threshold
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.product.thresholdQuantity")
    Iterable<Inventory> findLowInventory();
    
    // Query to check if product has sufficient inventory
    @Query("SELECT CASE WHEN i.quantity >= :quantity THEN true ELSE false END FROM Inventory i WHERE i.product.id = :productId")
    boolean hasSufficientInventory(@Param("productId") Long productId, @Param("quantity") int quantity);
}