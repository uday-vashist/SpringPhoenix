package com.example.legacy.controller;

import com.example.legacy.model.Product;
import com.example.legacy.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductController} — ProductService is mocked.
 */
@DisplayName("ProductController — Unit Tests")
class ProductControllerTest {

    private ProductService productService;
    private ProductController productController;

    @BeforeEach
    void setUp() {
        productService    = Mockito.mock(ProductService.class);
        productController = new ProductController(productService);
    }

    @Test
    @DisplayName("getAllProducts returns all products")
    void getAllProducts_returnsAll() {
        when(productService.getAllProducts())
                .thenReturn(Arrays.asList(
                        new Product("A", 1.0, 5),
                        new Product("B", 2.0, 10)));

        List<Product> result = productController.getAllProducts();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getProductById returns 200 OK when product found")
    void getProductById_found_returns200() {
        Product p = new Product("Widget", 9.99, 3);
        when(productService.getProductById(1L)).thenReturn(Optional.of(p));

        ResponseEntity<Product> response = productController.getProductById(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Widget");
    }

    @Test
    @DisplayName("getProductById returns 404 when product not found")
    void getProductById_notFound_returns404() {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Product> response = productController.getProductById(99L);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("getProductByName returns 200 OK when found")
    void getProductByName_found_returns200() {
        Product p = new Product("Gadget", 15.0, 7);
        when(productService.getProductByName("Gadget")).thenReturn(Optional.of(p));

        ResponseEntity<Product> response = productController.getProductByName("Gadget");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getPrice()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("getProductByName returns 404 when not found")
    void getProductByName_notFound_returns404() {
        when(productService.getProductByName("Ghost")).thenReturn(Optional.empty());

        ResponseEntity<Product> response = productController.getProductByName("Ghost");

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("getProductsCheaperThan returns matching products")
    void getProductsCheaperThan_returnsList() {
        when(productService.getProductsCheaperThan(10.0))
                .thenReturn(List.of(new Product("Cheap", 5.0, 20)));

        List<Product> result = productController.getProductsCheaperThan(10.0);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isLessThan(10.0);
    }

    @Test
    @DisplayName("createProduct returns saved product with 200 OK (legacy — not 201)")
    void createProduct_returns200WithBody() {
        Product p = new Product("NewItem", 5.0, 50);
        when(productService.saveProduct(any(Product.class))).thenReturn(p);

        Product result = productController.createProduct(p);

        assertThat(result.getName()).isEqualTo("NewItem");
    }

    @Test
    @DisplayName("deleteProduct returns 204 No Content")
    void deleteProduct_returns204() {
        doNothing().when(productService).deleteProduct(1L);

        ResponseEntity<Void> response = productController.deleteProduct(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }
}
