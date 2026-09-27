package com.example.legacy.repository;

import com.example.legacy.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProductRepository} using real H2 in-memory DB.
 * Uses @DataJpaTest — each test is rolled back on completion.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ProductRepository — Integration Tests")
class ProductRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        widget = entityManager.persistAndFlush(new Product("Widget", 9.99, 50));
        gadget = entityManager.persistAndFlush(new Product("Gadget", 24.99, 10));
    }

    @Test
    @DisplayName("findByName returns product when name exists")
    void findByName_existingName_returnsProduct() {
        Optional<Product> result = productRepository.findByName("Widget");

        assertThat(result).isPresent();
        assertThat(result.get().getPrice()).isEqualTo(9.99);
        assertThat(result.get().getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("findByName returns empty for unknown name")
    void findByName_unknownName_returnsEmpty() {
        assertThat(productRepository.findByName("NonExistent")).isEmpty();
    }

    @Test
    @DisplayName("findByPriceLessThan returns only products below threshold")
    void findByPriceLessThan_returnsMatchingProducts() {
        List<Product> cheap = productRepository.findByPriceLessThan(15.0);

        assertThat(cheap)
                .hasSize(1)
                .extracting(Product::getName)
                .containsExactly("Widget");
    }

    @Test
    @DisplayName("findByPriceLessThan returns empty list when none qualify")
    void findByPriceLessThan_noneQualify_returnsEmpty() {
        List<Product> result = productRepository.findByPriceLessThan(1.0);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll returns all persisted products")
    void findAll_returnsAllProducts() {
        assertThat(productRepository.findAll())
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Widget", "Gadget");
    }

    @Test
    @DisplayName("findById returns product for known ID")
    void findById_knownId_returnsProduct() {
        Optional<Product> result = productRepository.findById(widget.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Widget");
    }

    @Test
    @DisplayName("findById returns empty for unknown ID")
    void findById_unknownId_returnsEmpty() {
        assertThat(productRepository.findById(Long.MAX_VALUE)).isEmpty();
    }

    @Test
    @DisplayName("save persists new product and generates ID")
    void save_newProduct_persistsWithId() {
        Product gizmo = productRepository.save(new Product("Gizmo", 4.99, 200));

        assertThat(gizmo.getId()).isNotNull().isPositive();
        assertThat(productRepository.findByName("Gizmo")).isPresent();
    }

    @Test
    @DisplayName("save updates existing product fields")
    void save_existingProduct_updatesFields() {
        widget.setPrice(12.99);
        productRepository.save(widget);
        entityManager.flush();
        entityManager.clear();

        Product reloaded = productRepository.findById(widget.getId()).orElseThrow();
        assertThat(reloaded.getPrice()).isEqualTo(12.99);
    }

    @Test
    @DisplayName("deleteById removes the product")
    void deleteById_existingId_removesRecord() {
        productRepository.deleteById(widget.getId());
        entityManager.flush();

        assertThat(productRepository.findById(widget.getId())).isEmpty();
    }

    @Test
    @DisplayName("count reflects number of persisted products")
    void count_reflectsPersisted() {
        assertThat(productRepository.count()).isEqualTo(2L);
    }
}
