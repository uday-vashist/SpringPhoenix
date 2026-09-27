package com.example.legacy.service;

import com.example.legacy.model.Product;
import com.example.legacy.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for {@link Product} CRUD operations.
 *
 * Legacy baseline — deliberately missing:
 *   - @Transactional(readOnly = true) on class for read methods
 *   - @Transactional on individual write methods
 *   - updateProduct operation (no update endpoint exists)
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Legacy smell: no @Transactional(readOnly=true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> getProductsCheaperThan(Double maxPrice) {
        return productRepository.findByPriceLessThan(maxPrice);
    }

    // Legacy smell: no @Transactional — save is not atomic
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Legacy smell: no update operation — clients must delete + re-insert
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
