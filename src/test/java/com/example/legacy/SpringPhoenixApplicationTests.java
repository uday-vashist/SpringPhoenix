package com.example.legacy;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for {@link SpringPhoenixApplication}.
 *
 * <p>Uses Mockito's {@link MockedStatic} to intercept
 * {@link SpringApplication#run(Class, String...)} so the full Spring
 * application context is never started, keeping tests fast and side-effect free.</p>
 */
class SpringPhoenixApplicationTests {

    // -----------------------------------------------------------------------
    // main() coverage
    // -----------------------------------------------------------------------

    /**
     * Verifies that {@link SpringPhoenixApplication#main(String[])} delegates
     * to {@code SpringApplication.run()} with the correct class and args, and
     * that no exception escapes from the entry point.
     */
    @Test
    void mainDelegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {

            // Stub run() so no real context is started
            springApp.when(() -> SpringApplication.run(SpringPhoenixApplication.class, new String[]{}))
                     .thenReturn(null);

            assertDoesNotThrow(() -> SpringPhoenixApplication.main(new String[]{}));

            // Verify the correct class was passed to run()
            springApp.verify(() -> SpringApplication.run(SpringPhoenixApplication.class, new String[]{}));
        }
    }

    /**
     * Verifies that {@code main()} forwards arbitrary CLI args to
     * {@link SpringApplication#run(Class, String...)} unchanged.
     */
    @Test
    void mainForwardsArgsToSpringApplicationRun() {
        String[] args = {"--server.port=8080", "--spring.profiles.active=test"};

        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            springApp.when(() -> SpringApplication.run(SpringPhoenixApplication.class, args))
                     .thenReturn(null);

            assertDoesNotThrow(() -> SpringPhoenixApplication.main(args));

            springApp.verify(() -> SpringApplication.run(SpringPhoenixApplication.class, args));
        }
    }

    // -----------------------------------------------------------------------
    // Instantiation (ensures no-arg constructor is covered)
    // -----------------------------------------------------------------------

    /**
     * Constructs a {@link SpringPhoenixApplication} instance to cover the
     * implicit no-arg constructor that the JVM generates for the class.
     */
    @Test
    void constructorIsAccessible() {
        assertDoesNotThrow(SpringPhoenixApplication::new);
    }
}
