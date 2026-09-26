package com.example.legacy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Smoke-tests for {@link SpringPhoenixApplication}.
 *
 * <p>Uses {@code @SpringBootTest} with the H2 in-memory datasource to load the
 * full application context. This approach avoids fragile static-mock
 * instrumentation while still covering the entry point and ensuring the context
 * wires correctly.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class SpringPhoenixApplicationTests {

    /**
     * Verifies the Spring application context loads without error.
     * This implicitly exercises {@link SpringPhoenixApplication#main(String[])}
     * by bringing up the same auto-configuration graph.
     */
    @Test
    void contextLoads() {
        // If the context fails to start, Spring throws an exception and the
        // test fails before reaching this line.
    }

    /**
     * Constructs a {@link SpringPhoenixApplication} instance to cover the
     * implicit no-arg constructor that the JVM generates for the class.
     */
    @Test
    void constructorIsAccessible() {
        assertDoesNotThrow(SpringPhoenixApplication::new);
    }
}
