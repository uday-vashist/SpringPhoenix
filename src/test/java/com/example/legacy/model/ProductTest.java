package com.example.legacy.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Product} — covers constructor, getters/setters,
 * and documents the intentional lack of equals/hashCode override (legacy smell).
 */
@DisplayName("Product — Unit Tests")
class ProductTest {

    @Test
    @DisplayName("No-arg constructor creates instance with null fields")
    void noArgConstructor_nullFields() {
        Product p = new Product();
        assertThat(p.getId()).isNull();
        assertThat(p.getName()).isNull();
        assertThat(p.getPrice()).isNull();
        assertThat(p.getStock()).isNull();
    }

    @Test
    @DisplayName("Convenience constructor assigns all fields")
    void convenienceConstructor_assignsAllFields() {
        Product p = new Product("Widget", 9.99, 100);
        assertThat(p.getName()).isEqualTo("Widget");
        assertThat(p.getPrice()).isEqualTo(9.99);
        assertThat(p.getStock()).isEqualTo(100);
        assertThat(p.getId()).isNull();  // not yet persisted
    }

    @Test
    @DisplayName("Setters update all fields")
    void setters_updateAllFields() {
        Product p = new Product();
        p.setId(42L);
        p.setName("Gadget");
        p.setPrice(19.99);
        p.setStock(50);

        assertThat(p.getId()).isEqualTo(42L);
        assertThat(p.getName()).isEqualTo("Gadget");
        assertThat(p.getPrice()).isEqualTo(19.99);
        assertThat(p.getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("Legacy smell: two Products with same id are NOT equal (Object identity)")
    void legacySmell_noEqualsOverride_twoInstancesWithSameIdAreNotEqual() {
        Product a = new Product("Widget", 9.99, 10);
        a.setId(1L);
        Product b = new Product("Widget", 9.99, 10);
        b.setId(1L);

        // This is the legacy behaviour — without equals() override, identity is used.
        // After modernisation, this should become assertThat(a).isEqualTo(b).
        assertThat(a).isNotSameAs(b);
        // Documenting that equals falls back to Object.equals (identity):
        assertThat(a.equals(b)).isFalse();
    }

    @Test
    @DisplayName("Product is equal to itself")
    void sameInstance_isEqualToItself() {
        Product p = new Product("Widget", 9.99, 10);
        p.setId(1L);
        assertThat(p).isEqualTo(p);
    }

    @Test
    @DisplayName("Product allows negative price (missing @DecimalMin constraint)")
    void legacySmell_allowsNegativePrice() {
        Product p = new Product("Defective", -5.00, 0);
        // No exception thrown — validation constraint is absent in legacy code
        assertThat(p.getPrice()).isNegative();
    }

    @Test
    @DisplayName("Product allows zero stock")
    void zeroStockAllowed() {
        Product p = new Product("OutOfStock", 1.99, 0);
        assertThat(p.getStock()).isZero();
    }
}
