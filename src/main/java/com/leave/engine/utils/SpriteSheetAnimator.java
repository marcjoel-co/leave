package com.leave.engine.utils;

import java.net.URL;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Animates a sprite sheet on a JavaFX {@link ImageView}.
 * It cycles through frames of a sprite sheet image by updating the ImageView's viewport.
 * Supports looping and an on-finished callback for non-looping animations.
 */
public class SpriteSheetAnimator {

    private final ImageView imageView;
    private final Image spriteSheet;
    private final int frameWidth;
    private final int frameHeight;
    private final int numColsInSheet; // Renamed for clarity
    private final int totalFramesInSequence; // Renamed for clarity
    private final double fps;
    private final boolean loopAnimation;

    private int currentFrameIndex = 0; // Renamed for clarity
    private long lastFrameTimeNs = 0;    // Renamed for clarity (nanoseconds)
    private AnimationTimer animationTimer;
    private Runnable onFinishedCallback;
    private boolean isPlaying = false;

    /**
     * Main constructor for the SpriteSheetAnimator.
     *
     * @param imageView         The ImageView to display the animation on. Must not be null.
     * @param spriteSheetPath   The classpath resource path to the sprite sheet image (e.g., "/com/example/sprites.png").
     *                          The path should typically start with a "/" if it's absolute from the classpath root.
     * @param frameWidth        The width of a single frame in the sprite sheet. Must be positive.
     * @param frameHeight       The height of a single frame in the sprite sheet. Must be positive.
     * @param numColsInSheet    The number of columns of frames in the entire sprite sheet image. Must be positive.
     * @param totalFramesInSequence The total number of frames that make up this specific animation sequence. Must be positive.
     *                           (This allows using only a portion of a larger sprite sheet if needed).
     * @param fps               Desired frames per second for the animation. Must be positive.
     * @param loop              True if the animation sequence should loop indefinitely, false otherwise.
     * @throws IllegalArgumentException if any parameters are invalid (e.g., null ImageView, non-positive dimensions/fps)
     *                                  or if the sprite sheet image cannot be loaded from the given path.
     */
    public SpriteSheetAnimator(ImageView imageView, String spriteSheetPath,
                               int frameWidth, int frameHeight, int numColsInSheet,
                               int totalFramesInSequence, double fps, boolean loop) {
        if (imageView == null) { // test if the image does not exist
            throw new IllegalArgumentException("ImageView cannot be null.");
        }
        if (spriteSheetPath == null || spriteSheetPath.trim().isEmpty()) { // test if  path is empty, null or whitespace
            throw new IllegalArgumentException("Sprite sheet path cannot be null or empty.");
        }
        if (frameWidth <= 0 || frameHeight <= 0 || numColsInSheet <= 0 || totalFramesInSequence <= 0 || fps <= 0) { //
            throw new IllegalArgumentException("Frame dimensions, columns in sheet, total frames in sequence, and FPS must all be positive.");
        }

        this.imageView = imageView;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.numColsInSheet = numColsInSheet;
        this.totalFramesInSequence = totalFramesInSequence;
        this.fps = fps;
        this.loopAnimation = loop;

        URL spriteUrl = getClass().getResource(spriteSheetPath);
        if (spriteUrl == null && !spriteSheetPath.startsWith("/")) {
            spriteUrl = getClass().getResource("/" + spriteSheetPath); // Attempt with leading slash
        }
        if (spriteUrl == null) {
            throw new IllegalArgumentException("Sprite sheet resource not found at: " + spriteSheetPath);
        }

        this.spriteSheet = new Image(spriteUrl.toExternalForm());
        if (this.spriteSheet.isError()) {
            this.spriteSheet.getException().printStackTrace(); // Log the underlying image load error
            throw new IllegalArgumentException("Error loading sprite sheet image: " + spriteSheetPath, this.spriteSheet.getException());
        }

        // Validate image dimensions against frame dimensions (optional but good)
        if (this.spriteSheet.getWidth() < frameWidth || this.spriteSheet.getHeight() < frameHeight) {
            throw new IllegalArgumentException("Sprite sheet dimensions are smaller than a single frame's dimensions.");
        }

        this.imageView.setImage(this.spriteSheet);
        setFrame(0); // Display the first frame of the sequence initially
    }

    /**
     * Overloaded constructor that defaults to a non-looping animation.
     * @see #SpriteSheetAnimator(ImageView, String, int, int, int, int, double, boolean)
     */
    public SpriteSheetAnimator(ImageView imageView, String spriteSheetPath,
                               int frameWidth, int frameHeight, int numColsInSheet,
                               int totalFramesInSequence, double fps) {
        this(imageView, spriteSheetPath, frameWidth, frameHeight, numColsInSheet, totalFramesInSequence, fps, false);
    }

    /**
     * Sets the viewport of the ImageView to display the specified frame index.
     * Calculations are based on a sprite sheet arranged in rows and columns.
     * @param frameIndex The 0-based index of the frame to display from the animation sequence.
     */
    private void setFrame(int frameIndex) {
        if (spriteSheet == null || spriteSheet.isError() || imageView == null) {
            return;
        }
        // Validate frameIndex against totalFramesInSequence to prevent errors
        // The play() logic handles wrapping or stopping, but this is a safeguard for direct calls.
        if (frameIndex < 0 || frameIndex >= totalFramesInSequence) {
             System.err.println("SpriteSheetAnimator: Attempted to set invalid frame index: " + frameIndex + 
                                ", totalFramesInSequence: " + totalFramesInSequence + ". Clamping to valid range or doing nothing.");
            if (loopAnimation) {
                frameIndex = frameIndex % totalFramesInSequence; // Wrap if looping
                if (frameIndex < 0) frameIndex += totalFramesInSequence; // Ensure positive if result of % is negative
            } else {
                 frameIndex = Math.max(0, Math.min(frameIndex, totalFramesInSequence - 1)); // Clamp to bounds
            }
        }

        int col = frameIndex % numColsInSheet;
        int row = frameIndex / numColsInSheet; // Integer division gives the correct row index

        double x = col * (double) frameWidth;
        double y = row * (double) frameHeight;

        // Basic check to ensure calculated viewport is within the actual spritesheet dimensions
        if (x + frameWidth > spriteSheet.getWidth() || y + frameHeight > spriteSheet.getHeight()) {
            System.err.println("SpriteSheetAnimator Warning: Calculated viewport for frame " + frameIndex +
                               " (x:" + x + ", y:" + y + ") may extend beyond sprite sheet dimensions (" +
                               spriteSheet.getWidth() + "x" + spriteSheet.getHeight() + "). " +
                               "Check numColsInSheet, totalFramesInSequence, and frame dimensions.");
            // Don't set viewport if it's clearly out of bounds for the whole sheet
            // Though the primary error would be in parameter setup.
            // We could choose to clamp viewport dimensions here too, but it's better if parameters are correct.
        }
        
        imageView.setViewport(new Rectangle2D(x, y, frameWidth, frameHeight));
    }

    /**
     * Sets a callback to be executed when a non-looping animation completes naturally.
     * This callback is not invoked if the animation is stopped manually via {@link #stop()}.
     *
     * @param callback The {@link Runnable} to execute on completion.
     */
    public void setOnFinished(Runnable callback) {
        this.onFinishedCallback = callback;
    }

    /**
     * Starts playing the animation from the beginning (frame 0).
     * If the animation is already playing, this method does nothing.
     * If an {@link AnimationTimer} was previously active, it is stopped first.
     */
    public void play() {
        if (isPlaying) {
            return;
        }
        if (spriteSheet == null || spriteSheet.isError()) {
            System.err.println("SpriteSheetAnimator: Cannot play, spriteSheet is null or has an error.");
            return;
        }
        if (fps <= 0) { // Prevent division by zero or infinite loop if not caught in constructor
            System.err.println("SpriteSheetAnimator: FPS must be positive to play. Animation not started.");
            return;
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
        currentFrameIndex = 0;
        setFrame(currentFrameIndex);
        isPlaying = true;

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long nowNs) { // Parameter is in nanoseconds
                if (lastFrameTimeNs == 0) { // First frame after play()
                    lastFrameTimeNs = nowNs;
                    return;
                }

                long elapsedNs = nowNs - lastFrameTimeNs;
                long frameDurationNs = (long) (1_000_000_000.0 / fps);

                if (elapsedNs >= frameDurationNs) {
                    currentFrameIndex++;
                    if (currentFrameIndex >= totalFramesInSequence) {
                        if (loopAnimation) {
                            currentFrameIndex = 0; // Loop back to the first frame
                        } else {
                            this.stop(); // Stop this AnimationTimer instance
                            SpriteSheetAnimator.this.isPlaying = false; // Update outer class field
                            SpriteSheetAnimator.this.lastFrameTimeNs = 0; // Reset outer class field
                            if (onFinishedCallback != null) {
                                Platform.runLater(onFinishedCallback); // Ensure callback on FX thread
                            }
                            return; // Animation finished and not looping
                        }
                    }
                    setFrame(currentFrameIndex);
                    // Adjust lastFrameTime to account for potential overshoot, helps maintain smoother FPS
                    lastFrameTimeNs = nowNs - (elapsedNs % frameDurationNs);
                }
            }
        };
        lastFrameTimeNs = 0; // Reset for the new play session
        animationTimer.start();
    }

    /**
     * Stops the animation if it is currently playing.
     * Resets animation state. The onFinishedCallback is NOT called by this method.
     */
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        isPlaying = false;
        lastFrameTimeNs = 0;
        // currentFrameIndex could be reset to 0 here or left as is, depending on desired resume behavior
        // For now, let's leave currentFrameIndex, so a subsequent play() starts from 0.
    }

    /**
     * @return True if the animation is currently playing, false otherwise.
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Stops the current animation and jumps to a specific frame, displaying it statically.
     *
     * @param frameIndex The 0-based index of the frame to display.
     *                   If out of bounds, an error is printed, and no change occurs for invalid indices.
     */
    public void gotoAndStop(int frameIndex) {
        stop();
        if (frameIndex >= 0 && frameIndex < totalFramesInSequence) {
            currentFrameIndex = frameIndex;
            setFrame(currentFrameIndex);
        } else {
            System.err.println("SpriteSheetAnimator: gotoAndStop - Frame index " + frameIndex +
                               " is out of bounds (0-" + (totalFramesInSequence - 1) + ").");
        }
    }
}