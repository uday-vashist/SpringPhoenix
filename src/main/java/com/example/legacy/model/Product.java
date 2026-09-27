package com.example.legacy.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a product in the catalogue.
 *
 * Legacy baseline — deliberately missing:
 *   - Bean Validation constraints (@NotBlank, @DecimalMin)
 *   - @Column nullable=false / unique constraints
 *   - Proper equals/hashCode (falls back to Object identity)
 *   - toString override
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // no @NotBlank — legacy oversight
    private String name;

    // no @DecimalMin — price could be negative, legacy oversight
    private Double price;

    // stock stored as Integer, no @Min(0) constraint
    private Integer stock;

    // Required no-arg constructor for JPA
    public Product() {}

    // Convenience constructor
    public Product(String name, Double price, Integer stock) {
        this.name  = name;
        this.price = price;
        this.stock = stock;
    }

    public Long getId()            { return id; }
    public void setId(Long id)     { this.id = id; }

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public Double getPrice()                { return price; }
    public void setPrice(Double price)      { this.price = price; }

    public Integer getStock()               { return stock; }
    public void setStock(Integer stock)     { this.stock = stock; }

    // Legacy smell: no equals/hashCode override — uses Object identity.
    // This means two Product instances with the same id are NOT equal by value.
}
