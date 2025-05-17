package com.leave.engine.utils; // Corrected: Only one package declaration

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.Label; 
import javafx.util.Duration;

public class AnimationUtils {

    public static FadeTransition createFadeTransition(Node node, Duration duration, double fromOpacity, double toOpacity) {
        if (node == null) {
            System.err.println("Warning: Attempted to create FadeTransition for nothing.");
            return new FadeTransition();
        }
        FadeTransition ft = new FadeTransition(duration, node);
        ft.setFromValue(fromOpacity);
        ft.setToValue(toOpacity);
        return ft;
    }

    public static PauseTransition createPauseTransition(Duration duration) {
        return new PauseTransition(duration);
    }

    public static Timeline createBlinkTimeline(Node node, Duration segmentDuration, double finalOpacity) {
        if (node == null) {
            System.err.println("Warning: Attempted to create BlinkTimeline for nothing.");
            return new Timeline();
        }
        double singleSegmentMillis = segmentDuration.toMillis();
        return new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(singleSegmentMillis * 0.5), new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(singleSegmentMillis * 2), new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(singleSegmentMillis * 3), new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(singleSegmentMillis * 4), new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(singleSegmentMillis * 6), new KeyValue(node.opacityProperty(), finalOpacity))
        );
    }

    public static Timeline createSimpleBlinkTimeline(Node node, Duration onDuration, Duration offDuration, double finalOpacity) {
        if (node == null) {
            System.err.println("Warning: Attempted to create SimpleBlinkTimeline for nothing");
            return new Timeline();
        }
        return new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(10), new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(onDuration, new KeyValue(node.opacityProperty(), 1.0)),
            new KeyFrame(onDuration.add(offDuration), new KeyValue(node.opacityProperty(), 0.0)),
            new KeyFrame(onDuration.add(offDuration).add(Duration.millis(10)), new KeyValue(node.opacityProperty(), finalOpacity))
        );
    }

    // --- NEW TEXT ANIMATION METHODS ---

    /**
     * Animates text appearing character by character in a JavaFX Label.
     *
     * @param label         The Label to animate the text in.
     * @param fullText      The complete text to display.
     * @param charDelayMs   The delay in milliseconds between each character appearing.
     * @param onFinished    A Runnable to execute once the animation is complete (can be null).
     */
    public static void animateText(Label label, String fullText, int charDelayMs, Runnable onFinished) {
        // Stop any existing animation on this label
        Object oldTimelineObj = label.getProperties().get("activeTextAnimation");
        if (oldTimelineObj instanceof Timeline) {
            ((Timeline) oldTimelineObj).stop();
            label.getProperties().remove("activeTextAnimation"); // Clean up
        }

        if (fullText == null || fullText.isEmpty()) {
            label.setText("");
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        final StringBuilder displayedText = new StringBuilder();
        label.setText(""); // Clear existing text
        label.setUserData(fullText); // Store full text for potential completion

        final Timeline timeline = new Timeline();
        timeline.setCycleCount(fullText.length());
        label.getProperties().put("activeTextAnimation", timeline);

        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(charDelayMs),
                event -> {
                    if (displayedText.length() < fullText.length()) {
                        displayedText.append(fullText.charAt(displayedText.length()));
                        label.setText(displayedText.toString());
                    }
                }
        );

        timeline.getKeyFrames().add(keyFrame);
        timeline.setOnFinished(event -> {
            label.getProperties().remove("activeTextAnimation"); // Clean up
            if (onFinished != null) {
                onFinished.run();
            }
        });

        timeline.play();
    }

    /**
     * Overloaded method with a default character delay.
     */
    public static void animateText(Label label, String fullText, Runnable onFinished) {
        animateText(label, fullText, 50, onFinished); // Default 50ms delay
    }

    /**
     * Overloaded method with a default character delay and no onFinished action.
     */
    public static void animateText(Label label, String fullText) {
        animateText(label, fullText, 50, null); // Default 50ms delay
    }

    /**
     * Stops any ongoing text animation on the given label.
     * @param label The label to stop animation on.
     */
    public static void stopTextAnimation(Label label) {
        Object timelineObj = label.getProperties().get("activeTextAnimation");
        if (timelineObj instanceof Timeline) {
            Timeline timeline = (Timeline) timelineObj;
            timeline.stop();
            label.getProperties().remove("activeTextAnimation");
        }
    }

    /**
     * Completes any ongoing text animation on the given label, showing the full text immediately.
     * It retrieves the full text from the label's user data.
     * @param label The label to complete animation on.
     */
    public static void completeTextAnimation(Label label) {
        Object timelineObj = label.getProperties().get("activeTextAnimation");
        if (timelineObj instanceof Timeline) {
            ((Timeline) timelineObj).stop();
            label.getProperties().remove("activeTextAnimation");
        }
        // Retrieve the full text stored in user data
        if (label.getUserData() instanceof String) {
            label.setText((String) label.getUserData());
        }
        // else, if no user data or wrong type, it keeps the current text.
    }
}