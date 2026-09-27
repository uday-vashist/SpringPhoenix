package com.example.legacy.controller;

import com.example.legacy.model.Product;
import com.example.legacy.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link ProductController} using a full Spring Boot
 * context and MockMvc — no mocks anywhere in the stack.
 *
 * <p>Each test is wrapped in a transaction that rolls back on completion, so
 * every test starts from the clean state produced by {@link BeforeEach}.</p>
 *
 * <p>Note: the legacy {@link ProductController} returns HTTP 200 (not 201) on
 * POST and has no PUT endpoint — tests deliberately assert the <em>current</em>
 * (baseline) behaviour so the pipeline can detect regressions after
 * modernisation.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductController — Integration Tests (MockMvc)")
class ProductControllerIntegrationTest {

    private static final String BASE_URL = "/api/products";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product persistedProduct;

    @BeforeEach
    void setUp() {
        persistedProduct = productRepository.save(new Product("CtrlWidget", 29.99, 10));
    }

    // ─── GET /api/products ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/products returns 200 with JSON array containing seeded product")
    void getAllProducts_returns200WithList() throws Exception {
        mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.name == 'CtrlWidget')]").exists());
    }

    @Test
    @DisplayName("GET /api/products returns 200 with empty array when no products exist")
    void getAllProducts_emptyDb_returns200WithEmptyArray() throws Exception {
        productRepository.deleteAll();

        mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ─── GET /api/products/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/products/{id} returns 200 with full product JSON for a known ID")
    void getProductById_knownId_returns200WithProduct() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + persistedProduct.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(persistedProduct.getId()))
                .andExpect(jsonPath("$.name").value("CtrlWidget"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("GET /api/products/{id} returns 404 for an unknown ID")
    void getProductById_unknownId_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + Long.MAX_VALUE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/products/search?name= ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/products/search?name= returns 200 with product when name matches")
    void searchByName_knownName_returns200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("name", "CtrlWidget")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CtrlWidget"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    @DisplayName("GET /api/products/search?name= returns 404 when name does not match any product")
    void searchByName_unknownName_returns404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search")
                        .param("name", "Nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ─── GET /api/products/cheaper-than?maxPrice= ────────────────────────────

    @Test
    @DisplayName("GET /api/products/cheaper-than returns products below threshold")
    void cheaperThan_returnsOnlyCheapProducts() throws Exception {
        productRepository.save(new Product("PriceyItem", 99.99, 2));
        productRepository.save(new Product("BudgetItem", 5.00, 100));

        mockMvc.perform(get(BASE_URL + "/cheaper-than")
                        .param("maxPrice", "30.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'CtrlWidget')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'BudgetItem')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'PriceyItem')]").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/products/cheaper-than returns empty array when nothing is below threshold")
    void cheaperThan_nothingBelow_returnsEmptyArray() throws Exception {
        mockMvc.perform(get(BASE_URL + "/cheaper-than")
                        .param("maxPrice", "0.01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/products/cheaper-than returns all products when threshold is very high")
    void cheaperThan_highThreshold_returnsAll() throws Exception {
        mockMvc.perform(get(BASE_URL + "/cheaper-than")
                        .param("maxPrice", "9999.0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    // ─── POST /api/products ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/products returns 200 OK (legacy — not 201) with saved product body")
    void createProduct_validPayload_returns200WithBody() throws Exception {
        String json = objectMapper.writeValueAsString(new Product("NewProduct", 12.50, 30));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())           // legacy: 200, not 201
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("NewProduct"))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.stock").value(30));
    }

    @Test
    @DisplayName("POST /api/products persists the product in the database")
    void createProduct_persistsToDatabase() throws Exception {
        String json = objectMapper.writeValueAsString(new Product("PersistedGadget", 8.00, 5));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        assertThat(productRepository.findByName("PersistedGadget")).isPresent();
    }

    @Test
    @DisplayName("POST /api/products increments the total product count")
    void createProduct_incrementsCount() throws Exception {
        long before = productRepository.count();
        String json = objectMapper.writeValueAsString(new Product("CountGadget", 3.00, 1));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        assertThat(productRepository.count()).isEqualTo(before + 1);
    }

    // ─── DELETE /api/products/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/products/{id} returns 204 No Content for a known ID")
    void deleteProduct_knownId_returns204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + persistedProduct.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} removes the product from the database")
    void deleteProduct_removesRecordFromDatabase() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + persistedProduct.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(persistedProduct.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/products/{id} returns 204 even for a non-existent ID (legacy behaviour)")
    void deleteProduct_unknownId_returns204Legacy() throws Exception {
        // Legacy smell: controller always returns 204, does not distinguish missing vs found
        mockMvc.perform(delete(BASE_URL + "/" + Long.MAX_VALUE))
                .andExpect(status().isNoContent());
    }
}
