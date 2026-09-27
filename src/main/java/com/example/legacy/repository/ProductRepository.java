package com.example.legacy.repository;

import com.example.legacy.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    // Legacy smell: returns all products with price below threshold —
    // no pagination support, could be a large unbounded result set.
    List<Product> findByPriceLessThan(Double price);
}
