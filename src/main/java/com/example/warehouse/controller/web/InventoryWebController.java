package com.example.warehouse.controller.web;

import com.example.warehouse.model.Inventory;
import com.example.warehouse.model.Product;
import com.example.warehouse.service.InventoryService;
import com.example.warehouse.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryWebController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    @GetMapping
    public String getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String stockStatus,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        List<Inventory> inventoryItems = inventoryService.getAllInventory();
        
        // Filter by search term if specified
        if (searchTerm != null && !searchTerm.isEmpty()) {
            // Simple filtering by product name or SKU
            inventoryItems = inventoryItems.stream()
                    .filter(i -> i.getProduct().getName().toLowerCase().contains(searchTerm.toLowerCase()) || 
                                 i.getProduct().getSku().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
            model.addAttribute("searchTerm", searchTerm);
        }
        
        // Filter by stock status if specified
        if (stockStatus != null && !stockStatus.isEmpty()) {
            switch (stockStatus) {
                case "low":
                    inventoryItems = inventoryItems.stream()
                            .filter(i -> i.getQuantity() <= i.getProduct().getThresholdQuantity() && i.getQuantity() > 0)
                            .collect(Collectors.toList());
                    break;
                case "out":
                    inventoryItems = inventoryItems.stream()
                            .filter(i -> i.getQuantity() == 0)
                            .collect(Collectors.toList());
                    break;
                case "normal":
                    inventoryItems = inventoryItems.stream()
                            .filter(i -> i.getQuantity() > i.getProduct().getThresholdQuantity())
                            .collect(Collectors.toList());
                    break;
            }
            model.addAttribute("stockStatus", stockStatus);
        }
        
        // Pagination info
        int totalItems = inventoryItems.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // Paginate the inventory items manually
        int start = page * size;
        int end = Math.min(start + size, totalItems);
        List<Inventory> pagedInventoryItems = inventoryItems.subList(start < totalItems ? start : 0, end < totalItems ? end : totalItems);
        
        // Count items by status
        long inStockCount = inventoryItems.stream()
                .filter(i -> i.getQuantity() > i.getProduct().getThresholdQuantity())
                .count();
        
        long lowStockCount = inventoryItems.stream()
                .filter(i -> i.getQuantity() <= i.getProduct().getThresholdQuantity() && i.getQuantity() > 0)
                .count();
        
        long outOfStockCount = inventoryItems.stream()
                .filter(i -> i.getQuantity() == 0)
                .count();
        
        model.addAttribute("inventoryItems", pagedInventoryItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("inStockCount", inStockCount);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);

        return "inventory/list";
    }

    @GetMapping("/low")
    public String getLowInventory(Model model) {
        List<Inventory> lowInventoryItems = inventoryService.getLowInventory();
        model.addAttribute("inventoryItems", lowInventoryItems);
        model.addAttribute("stockStatus", "low");
        
        // Count items by status (reusing the same counts for consistency)
        long inStockCount = inventoryService.getAllInventory().stream()
                .filter(i -> i.getQuantity() > i.getProduct().getThresholdQuantity())
                .count();
        
        long lowStockCount = lowInventoryItems.size();
        
        long outOfStockCount = inventoryService.getAllInventory().stream()
                .filter(i -> i.getQuantity() == 0)
                .count();
        
        model.addAttribute("inStockCount", inStockCount);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        
        return "inventory/list";
    }

    @GetMapping("/product/{productId}/edit")
    public String showEditForm(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId);
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        
        model.addAttribute("product", product);
        model.addAttribute("inventory", inventory);
        model.addAttribute("action", "edit");
        
        return "inventory/form";
    }

    @GetMapping("/product/{productId}/add")
    public String showAddForm(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId);
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        
        model.addAttribute("product", product);
        model.addAttribute("inventory", inventory);
        model.addAttribute("action", "add");
        
        return "inventory/form";
    }

    @GetMapping("/product/{productId}/reduce")
    public String showReduceForm(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId);
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        
        model.addAttribute("product", product);
        model.addAttribute("inventory", inventory);
        model.addAttribute("action", "reduce");
        
        return "inventory/form";
    }

    @PostMapping("/product/{productId}/edit/submit")
    public String updateInventory(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        try {
            inventoryService.updateInventoryQuantity(productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Inventory updated successfully");
            return "redirect:/inventory";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating inventory: " + e.getMessage());
            return "redirect:/inventory/product/" + productId + "/edit";
        }
    }

    @PostMapping("/product/{productId}/add/submit")
    public String addInventory(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        try {
            inventoryService.addInventoryQuantity(productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Inventory quantity added successfully");
            return "redirect:/inventory";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding inventory: " + e.getMessage());
            return "redirect:/inventory/product/" + productId + "/add";
        }
    }

    @PostMapping("/product/{productId}/reduce/submit")
    public String reduceInventory(
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestParam String reason,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {
        
        try {
            inventoryService.reduceInventoryQuantity(productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Inventory quantity reduced successfully");
            return "redirect:/inventory";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error reducing inventory: " + e.getMessage());
            return "redirect:/inventory/product/" + productId + "/reduce";
        }
    }
}