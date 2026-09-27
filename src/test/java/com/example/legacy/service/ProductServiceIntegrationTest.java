package com.example.legacy.service;

import com.example.legacy.model.Product;
import com.example.legacy.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProductService} using a real Spring context and
 * an H2 in-memory database — no mocks anywhere in the stack.
 *
 * <p>Each test runs inside a transaction rolled back on completion so every
 * test starts from the state established by {@link BeforeEach}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductService — Integration Tests")
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Product persisted;

    @BeforeEach
    void setUp() {
        // One product in DB before every test.
        persisted = productService.saveProduct(new Product("IntegrationWidget", 19.99, 50));
    }

    // ─── getAllProducts ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllProducts returns at least the seeded product")
    void getAllProducts_returnsSeededProduct() {
        List<Product> all = productService.getAllProducts();

        assertThat(all)
                .hasSizeGreaterThanOrEqualTo(1)
                .extracting(Product::getName)
                .contains("IntegrationWidget");
    }

    @Test
    @DisplayName("getAllProducts returns empty list when no products exist")
    void getAllProducts_emptyDatabase_returnsEmptyList() {
        productRepository.deleteAll();

        assertThat(productService.getAllProducts()).isEmpty();
    }

    // ─── getProductById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getProductById returns present Optional for a known ID")
    void getProductById_knownId_returnsPresent() {
        Optional<Product> result = productService.getProductById(persisted.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("IntegrationWidget");
        assertThat(result.get().getPrice()).isEqualTo(19.99);
        assertThat(result.get().getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("getProductById returns empty Optional for an unknown ID")
    void getProductById_unknownId_returnsEmpty() {
        assertThat(productService.getProductById(Long.MAX_VALUE)).isEmpty();
    }

    // ─── getProductByName ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getProductByName returns product when name matches")
    void getProductByName_matchingName_returnsProduct() {
        Optional<Product> result = productService.getProductByName("IntegrationWidget");

        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualTo(19.99);
    }

    @Test
    @DisplayName("getProductByName returns empty Optional when no product has that name")
    void getProductByName_noMatch_returnsEmpty() {
        assertThat(productService.getProductByName("NonExistentProduct")).isEmpty();
    }

    // ─── getProductsCheaperThan ───────────────────────────────────────────────

    @Test
    @DisplayName("getProductsCheaperThan returns only products below the threshold")
    void getProductsCheaperThan_returnsOnlyCheapProducts() {
        productService.saveProduct(new Product("ExpensiveItem", 99.99, 5));
        productService.saveProduct(new Product("CheapItem", 4.99, 100));

        List<Product> result = productService.getProductsCheaperThan(20.0);

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(p -> assertThat(p.getPrice()).isLessThan(20.0));
        assertThat(result).extracting(Product::getName).contains("IntegrationWidget", "CheapItem");
        assertThat(result).extracting(Product::getName).doesNotContain("ExpensiveItem");
    }

    @Test
    @DisplayName("getProductsCheaperThan returns empty list when no product is below threshold")
    void getProductsCheaperThan_nothingCheap_returnsEmpty() {
        List<Product> result = productService.getProductsCheaperThan(1.0);

        assertThat(result).isEmpty();
    }

    // ─── saveProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveProduct persists product and assigns a generated ID")
    void saveProduct_newProduct_persistsWithGeneratedId() {
        Product p = productService.saveProduct(new Product("NewGadget", 7.50, 200));

        assertThat(p.getId()).isNotNull().isPositive();
        assertThat(productRepository.findByName("NewGadget")).isPresent();
    }

    @Test
    @DisplayName("saveProduct stores all fields correctly")
    void saveProduct_fieldsArePersistedCorrectly() {
        Product saved = productService.saveProduct(new Product("FieldCheck", 12.34, 77));

        assertThat(saved.getName()).isEqualTo("FieldCheck");
        assertThat(saved.getPrice()).isEqualTo(12.34);
        assertThat(saved.getStock()).isEqualTo(77);
    }

    @Test
    @DisplayName("saveProduct increments total product count")
    void saveProduct_incrementsTotalCount() {
        long before = productRepository.count();

        productService.saveProduct(new Product("CountTest", 1.0, 1));

        assertThat(productRepository.count()).isEqualTo(before + 1);
    }

    // ─── deleteProduct ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteProduct removes the product from the database")
    void deleteProduct_existingId_removesRecord() {
        productService.deleteProduct(persisted.getId());

        assertThat(productRepository.findById(persisted.getId())).isEmpty();
    }

    @Test
    @DisplayName("deleteProduct decrements the total product count")
    void deleteProduct_decrementsCount() {
        long before = productRepository.count();

        productService.deleteProduct(persisted.getId());

        assertThat(productRepository.count()).isEqualTo(before - 1);
    }
}
