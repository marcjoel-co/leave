package com.leave.engine.utils; // Package declaration MUST match the directory structure

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*; // For assertion methods like assertEquals, assertNotNull, etc.

// We might need these for testing JavaFX properties/nodes, but not immediately.
// Let's hold off on TestFX or headless Monocle setup for now,
// as many of these methods return JavaFX objects or manipulate them.
// For some tests, we might need to initialize the JavaFX toolkit.
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane; // A simple Node for testing
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;

import org.junit.jupiter.api.BeforeAll;

class AnimationUtilsTest {

    // This is needed to initialize the JavaFX Toolkit for tests that create/manipulate JavaFX Nodes/Animations
    // without launching a full Application.
    @BeforeAll
    static void initJavaFX() {
        // A common approach is to try to initialize it if it hasn't been already.
        // This can be a bit tricky to get right for all environments.
        // For simple tests, Platform.startup might be enough if it hasn't run.
        // However, repeated calls to startup() can cause issues.
        // A more robust solution for complex UI tests involves TestFX.
        // For now, let's assume tests might need the toolkit.
        try {
            Platform.startup(() -> {
                // This runnable is executed once JavaFX toolkit is ready
                System.out.println("JavaFX Toolkit initialized for tests.");
            });
        } catch (IllegalStateException e) {
            // Toolkit already initialized, which is fine.
            System.out.println("JavaFX Toolkit was already initialized.");
        }
    }

    // --- Test Methods will go here ---

    @Test
    void testCreateFadeTransition_withValidNode() {
        // Arrange
        Pane node = new Pane(); // A simple Node to test with
        Duration duration = Duration.millis(500);
        double fromOpacity = 0.0;
        double toOpacity = 1.0;

        // Act
        FadeTransition ft = AnimationUtils.createFadeTransition(node, duration, fromOpacity, toOpacity);

        // Assert
        assertNotNull(ft, "FadeTransition should not be null");
        assertEquals(node, ft.getNode(), "FadeTransition node should be the one provided");
        assertEquals(duration, ft.getDuration(), "FadeTransition duration should match");
        assertEquals(fromOpacity, ft.getFromValue(), "FadeTransition fromValue should match");
        assertEquals(toOpacity, ft.getToValue(), "FadeTransition toValue should match");
    }

    @Test
    void testCreateFadeTransition_withNullNode() {
        // Arrange
        Duration duration = Duration.millis(500);
        double fromOpacity = 0.0;
        double toOpacity = 1.0;

        // Act
        FadeTransition ft = AnimationUtils.createFadeTransition(null, duration, fromOpacity, toOpacity);

        // Assert
        assertNotNull(ft, "FadeTransition should still return a (default) transition object even for null node");
        // We might also check if System.err was called, but that's more advanced (e.g., capturing output streams).
        // For now, let's verify it returns a non-null default FadeTransition as per your code.
        assertNull(ft.getNode(), "FadeTransition node should be null as per its default constructor if input node was null and method returned 'new FadeTransition()'");
    }

    @Test
    void testCreatePauseTransition() {
        // Arrange
        Duration duration = Duration.seconds(1);

        // Act
        PauseTransition pt = AnimationUtils.createPauseTransition(duration);

        // Assert
        assertNotNull(pt, "PauseTransition should not be null");
        assertEquals(duration, pt.getDuration(), "PauseTransition duration should match");
    }

    // More tests will go here for other methods...
}