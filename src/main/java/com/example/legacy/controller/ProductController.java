package com.example.legacy.controller;

import com.example.legacy.model.Product;
import com.example.legacy.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing CRUD endpoints for {@link Product} resources.
 *
 * Legacy baseline — deliberately missing:
 *   - @Valid on request bodies (no Bean Validation triggered)
 *   - HTTP 201 Created on POST (returns 200 OK bare entity)
 *   - PUT update endpoint (no update operation exists)
 *   - Proper 404 handling on delete (always returns 204 even if not found)
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Product> getProductByName(@RequestParam String name) {
        return productService.getProductByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cheaper-than")
    public List<Product> getProductsCheaperThan(@RequestParam Double maxPrice) {
        return productService.getProductsCheaperThan(maxPrice);
    }

    // Legacy smell: POST returns 200 OK, not 201 Created; no @Valid
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    // Legacy smell: always 204 even when product doesn't exist
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
