package com.leave.engine.utils;

import java.net.URL;

import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SpriteSheetAnimator {

    private final ImageView imageView;
    private final Image spriteSheet;
    private final int frameWidth;
    private final int frameHeight;
    private final int numCols;
    private final int totalFrames;
    private final double fps;
    private final boolean loopAnimation; // Field to store the looping preference

    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private AnimationTimer animationTimer;
    private Runnable onFinishedCallback;
    private boolean isPlaying = false;

    /**
     * Main constructor for the SpriteSheetAnimator.
     *
     * @param imageView       The ImageView to display the animation
     * @param spriteSheetPath Path to the spritesheet image in resources (e.g., "/com/leave/engine/images/sheet.png").
     * @param frameWidth      Width of a single frame.
     * @param frameHeight     Height of a single frame.
     * @param numCols         Number of columns in the spritesheet.
     * @param totalFrames     Total number of frames in the animation sequence.
     * @param fps             Desired frames per second.
     * @param loop            True if the animation should loop indefinitely, false otherwise.
     * @throws IllegalArgumentException if parameters are invalid or the spritesheet cannot be loaded.
     */
    public SpriteSheetAnimator(ImageView imageView, String spriteSheetPath,
                               int frameWidth, int frameHeight, int numCols,
                               int totalFrames, double fps, boolean loop) { // Added 'loop' parameter
        if (imageView == null) {
            throw new IllegalArgumentException("ImageView cannot be null.");
        }
        if (spriteSheetPath == null || spriteSheetPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Sprite sheet path cannot be null or empty.");
        }
        if (frameWidth <= 0 || frameHeight <= 0 || numCols <= 0 || totalFrames <= 0 || fps <= 0) {
            throw new IllegalArgumentException("Frame dimensions, columns, total frames, and FPS must be positive.");
        }

        this.imageView = imageView;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.numCols = numCols;
        this.totalFrames = totalFrames;
        this.fps = fps;
        this.loopAnimation = loop; // Store the loop preference

        // Robust resource loading
        URL spriteUrl = getClass().getResource(spriteSheetPath);
        if (spriteUrl == null && !spriteSheetPath.startsWith("/")) {
            // Try again with a leading slash if it's intended to be absolute from classpath root
            spriteUrl = getClass().getResource("/" + spriteSheetPath);
        }
        if (spriteUrl == null) {
            throw new IllegalArgumentException("Sprite sheet not found at: " + spriteSheetPath + " (or with leading /)");
        }

        this.spriteSheet = new Image(spriteUrl.toExternalForm());
        if (this.spriteSheet.isError()) {
            // It's good to print the underlying image loading exception
            this.spriteSheet.getException().printStackTrace();
            throw new IllegalArgumentException("Error loading sprite sheet image: " + spriteSheetPath, this.spriteSheet.getException());
        }

        this.imageView.setImage(this.spriteSheet);
        setFrame(0); // Display the first frame initially
    }

    /**
     * Overloaded constructor that defaults to not looping.
     */
    public SpriteSheetAnimator(ImageView imageView, String spriteSheetPath,
                               int frameWidth, int frameHeight, int numCols,
                               int totalFrames, double fps) {
        this(imageView, spriteSheetPath, frameWidth, frameHeight, numCols, totalFrames, fps, false);
    }


    private void setFrame(int frameIndex) {
        if (spriteSheet == null || spriteSheet.isError() || imageView == null) {
            // Optionally log or handle error, but returning should prevent NullPointerExceptions
            return;
        }

        // Ensure frameIndex is within bounds if not looping, or wraps around if it is (though currentFrame handles wrapping)
        if (frameIndex < 0 || frameIndex >= totalFrames) {
             System.err.println("SpriteSheetAnimator: Attempted to set invalid frame index: " + frameIndex + ", totalFrames: " + totalFrames);
             if (!loopAnimation && frameIndex >=totalFrames) frameIndex = totalFrames -1; // Show last frame if out of bounds and not looping
             else if (loopAnimation) frameIndex = frameIndex % totalFrames; // Wrap if looping
             else return; // If negative and not looping, could be an issue.
        }


        int col = frameIndex % numCols;
        int row = frameIndex / numCols;

        double x = col * (double)frameWidth; // Cast to double for precision if frameWidth could be fractional due to sheet size
        double y = row * (double)frameHeight;

        imageView.setViewport(new Rectangle2D(x, y, frameWidth, frameHeight));

        // These are usually not needed unless you experience specific caching/rendering artifacts.
        // Disabling cache frequently can be a performance hit.
        // imageView.setCache(false);
        // imageView.setClip(null);
        // imageView.getTransforms().clear();
        // if (imageView.getParent() != null) {
        //     imageView.getParent().requestLayout();
        // }
    }

    public void setOnFinished(Runnable callback) {
        this.onFinishedCallback = callback;
    }

    public void play() {
        if (isPlaying) {
            return;
        }
        if (spriteSheet == null || spriteSheet.isError()) {
            System.err.println("SpriteSheetAnimator: Cannot play, spriteSheet is null or has an error.");
            return;
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
        currentFrame = 0;
        setFrame(currentFrame);
        isPlaying = true;

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }

                // Ensure fps is positive to avoid division by zero or negative delays
                if (fps <= 0) {
                    System.err.println("SpriteSheetAnimator: FPS is not positive, stopping animation.");
                    stop(); // Call the class's stop method
                    return;
                }
                
                long timeSinceLastFrame = now - lastFrameTime;
                long requiredDelayNanos = (long) (1_000_000_000.0 / fps);

                if (timeSinceLastFrame >= requiredDelayNanos) {
                    currentFrame++;
                    if (currentFrame >= totalFrames) {
                        if (loopAnimation) { // <<< USE THE STORED 'loopAnimation' FIELD
                            currentFrame = 0; // Reset to the first frame for looping
                        } else {
                            // Stop the AnimationTimer itself for non-looping animations
                            this.stop(); // 'this' refers to the AnimationTimer instance
                            isPlaying = false; // Update playing state
                            lastFrameTime = 0; // Reset time
                            if (onFinishedCallback != null) {
                                onFinishedCallback.run();
                            }
                            return; // Exit handle() as the non-looping animation is done
                        }
                    }
                    setFrame(currentFrame);
                    lastFrameTime = now; // Update last frame time
                }
            }
        };
        lastFrameTime = 0; // Reset for the new play session
        animationTimer.start();
    }

    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop(); // Stop the AnimationTimer
        }
        isPlaying = false;
        lastFrameTime = 0; // Reset time
        // Do not call onFinishedCallback here, as stop() can be called externally.
        // onFinishedCallback is only for natural completion of non-looping animation.
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void gotoAndStop(int frameIndex) {
        stop(); // Stop any current animation
        if (frameIndex >= 0 && frameIndex < totalFrames) {
            currentFrame = frameIndex; // Set the current frame index
            setFrame(currentFrame);   // Display that frame
        } else {
            System.err.println("SpriteSheetAnimator: Frame index " + frameIndex + " is out of bounds (0-" + (totalFrames - 1) + ").");
        }
    }
}