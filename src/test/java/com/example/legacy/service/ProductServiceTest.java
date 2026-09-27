package com.example.legacy.service;

import com.example.legacy.model.Product;
import com.example.legacy.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductService} — all repository calls are mocked.
 */
@DisplayName("ProductService — Unit Tests")
class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        productService    = new ProductService(productRepository);
    }

    @Test
    @DisplayName("getAllProducts returns all products from repository")
    void getAllProducts_returnsAll() {
        when(productRepository.findAll())
                .thenReturn(Arrays.asList(
                        new Product("A", 1.0, 10),
                        new Product("B", 2.0, 20)));

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("getProductById returns present Optional when product exists")
    void getProductById_found_returnsPresent() {
        Product p = new Product("Widget", 9.99, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Product> result = productService.getProductById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Widget");
    }

    @Test
    @DisplayName("getProductById returns empty Optional when product not found")
    void getProductById_notFound_returnsEmpty() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(productService.getProductById(99L)).isEmpty();
    }

    @Test
    @DisplayName("getProductByName delegates to repository findByName")
    void getProductByName_delegatesToRepository() {
        Product p = new Product("Gadget", 15.0, 3);
        when(productRepository.findByName("Gadget")).thenReturn(Optional.of(p));

        assertThat(productService.getProductByName("Gadget"))
                .isPresent()
                .hasValueSatisfying(prod -> assertThat(prod.getPrice()).isEqualTo(15.0));
    }

    @Test
    @DisplayName("getProductsCheaperThan returns products below threshold")
    void getProductsCheaperThan_returnsCheapProducts() {
        List<Product> cheap = List.of(new Product("Cheap", 5.0, 10));
        when(productRepository.findByPriceLessThan(10.0)).thenReturn(cheap);

        List<Product> result = productService.getProductsCheaperThan(10.0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isLessThan(10.0);
    }

    @Test
    @DisplayName("saveProduct delegates to repository save and returns saved entity")
    void saveProduct_delegatesAndReturns() {
        Product p = new Product("NewItem", 3.50, 100);
        when(productRepository.save(any(Product.class))).thenReturn(p);

        Product saved = productService.saveProduct(p);

        assertThat(saved.getName()).isEqualTo("NewItem");
        verify(productRepository).save(p);
    }

    @Test
    @DisplayName("deleteProduct delegates to repository deleteById")
    void deleteProduct_delegatesToRepository() {
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }
}
