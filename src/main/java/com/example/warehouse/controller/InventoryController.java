package com.example.warehouse.controller;

import com.example.warehouse.model.Inventory;
import com.example.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @GetMapping("/low")
    public ResponseEntity<List<Inventory>> getLowInventory() {
        return ResponseEntity.ok(inventoryService.getLowInventory());
    }

    @PutMapping("/product/{productId}/quantity/{quantity}")
    public ResponseEntity<Inventory> updateInventoryQuantity(
            @PathVariable Long productId,
            @PathVariable int quantity) {
        return ResponseEntity.ok(inventoryService.updateInventoryQuantity(productId, quantity));
    }

    @PutMapping("/product/{productId}/add/{quantity}")
    public ResponseEntity<Inventory> addInventoryQuantity(
            @PathVariable Long productId,
            @PathVariable int quantity) {
        return ResponseEntity.ok(inventoryService.addInventoryQuantity(productId, quantity));
    }

    @PutMapping("/product/{productId}/reduce/{quantity}")
    public ResponseEntity<Inventory> reduceInventoryQuantity(
            @PathVariable Long productId,
            @PathVariable int quantity) {
        return ResponseEntity.ok(inventoryService.reduceInventoryQuantity(productId, quantity));
    }

    @GetMapping("/product/{productId}/check/{quantity}")
    public ResponseEntity<Boolean> hasSufficientInventory(
            @PathVariable Long productId,
            @PathVariable int quantity) {
        return ResponseEntity.ok(inventoryService.hasSufficientInventory(productId, quantity));
    }
}