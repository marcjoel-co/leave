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
 * Optimized for single-frame sequences to act as static image display without continuous timer.
 */
public class SpriteSheetAnimator {

    private final ImageView imageView;
    private final Image spriteSheet;
    private final int frameWidth;
    private final int frameHeight;
    private final int numColsInSheet;
    private int totalFramesInSequence;
    private final double fps;
    private final boolean loopAnimation;

    private int currentFrameIndex = 0;
    private long lastFrameTimeNs = 0;
    private AnimationTimer animationTimer;
    private Runnable onFinishedCallback;
    private boolean isPlaying = false; 

    /**
     * Main constructor for the SpriteSheetAnimator.
     *
     * @param imageView             The ImageView to display the animation on.
     * @param spriteSheetPath       The classpath resource path to the sprite sheet image.
     * @param frameWidth            The width of a single frame.
     * @param frameHeight           The height of a single frame.
     * @param numColsInSheet        Number of columns of frames in the entire sprite sheet.
     * @param totalFramesInSequence Total number of frames in this animation sequence.
     * @param fps                   Desired frames per second.
     * @param loop                  True if the animation should loop.
     * @throws IllegalArgumentException if parameters are invalid or image cannot be loaded.
     */
    public SpriteSheetAnimator(ImageView imageView, String spriteSheetPath,
                               int frameWidth, int frameHeight, int numColsInSheet,
                               int totalFramesInSequence, double fps, boolean loop) {
        if (imageView == null) {
            throw new IllegalArgumentException("ImageView cannot be null.");
        }
        if (spriteSheetPath == null || spriteSheetPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Sprite sheet path cannot be null or empty.");
        }
        if (frameWidth <= 0 || frameHeight <= 0 || numColsInSheet <= 0 || totalFramesInSequence <= 0 || fps <= 0.0) { // Allow fps to be very small but still positive
             // Special case for fps: If totalFramesInSequence is 1, fps can be anything positive but doesn't really matter.
            if (!(totalFramesInSequence == 1 && fps > 0.0)) {
                 throw new IllegalArgumentException("Frame dimensions, columns, total frames, and FPS must be positive. (FPS must be > 0 even for single frame).");
            }
        }


        this.imageView = imageView;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.numColsInSheet = numColsInSheet;
        this.totalFramesInSequence = totalFramesInSequence;
        this.fps = (totalFramesInSequence == 1) ? 1.0 : fps; 
        this.loopAnimation = (totalFramesInSequence == 1) ? false : loop; // Loop is false for single frame

        URL spriteUrl = getClass().getResource(spriteSheetPath);
        if (spriteUrl == null && !spriteSheetPath.startsWith("/")) {
            spriteUrl = getClass().getResource("/" + spriteSheetPath);
        }
        if (spriteUrl == null) {
            throw new IllegalArgumentException("Sprite sheet resource not found at: " + spriteSheetPath);
        }

        this.spriteSheet = new Image(spriteUrl.toExternalForm());
        if (this.spriteSheet.isError()) {
            this.spriteSheet.getException().printStackTrace();
            throw new IllegalArgumentException("Error loading sprite sheet image: " + spriteSheetPath, this.spriteSheet.getException());
        }

        if (this.spriteSheet.getWidth() < frameWidth || this.spriteSheet.getHeight() < frameHeight) {
            throw new IllegalArgumentException("Sprite sheet dimensions (" + this.spriteSheet.getWidth() + "x" + this.spriteSheet.getHeight() +
                                               ") are smaller than a single frame's dimensions (" + frameWidth + "x" + frameHeight + "). " +
                                               "Path: " + spriteSheetPath);
        }
        // Ensure total frames doesn't exceed what's possible with the sheet dimensions
        int maxPossibleFrames = (int)(this.spriteSheet.getWidth() / frameWidth) * (int)(this.spriteSheet.getHeight() / frameHeight);
        if (totalFramesInSequence > maxPossibleFrames) {
             System.err.println("SpriteSheetAnimator Warning: totalFramesInSequence (" + totalFramesInSequence +
                                ") exceeds max possible frames (" + maxPossibleFrames + ") for the given sheet and frame dimensions. " +
                                "Path: " + spriteSheetPath + ". Clamping totalFramesInSequence.");
            this.totalFramesInSequence = maxPossibleFrames;
        }


        this.imageView.setImage(this.spriteSheet);
        // Do NOT call setFrame(0) here yet if totalFramesInSequence is 1, play() will handle it.
        // For multi-frame, play() will set the first frame.
        // If you want the first frame to always show immediately upon construction before play():
        if (this.totalFramesInSequence > 0) { // Ensure there's at least one frame possible
             setFrame(0);
        }
    }

    /**
     * Overloaded constructor defaults to non-looping.
     */
    public SpriteSheetAnimator(ImageView imageView, String spriteSheetPath,
                               int frameWidth, int frameHeight, int numColsInSheet,
                               int totalFramesInSequence, double fps) {
        this(imageView, spriteSheetPath, frameWidth, frameHeight, numColsInSheet, totalFramesInSequence, fps, false);
    }

    private void setFrame(int frameIndex) {
        if (spriteSheet == null || spriteSheet.isError() || imageView == null || totalFramesInSequence == 0) {
            return;
        }
        
        // Normalize frameIndex for display based on sequence length
        // This handles direct calls to setFrame that might be outside the play logic's management
        int displayIndex = frameIndex;
        if (displayIndex < 0 || displayIndex >= totalFramesInSequence) {
            if (loopAnimation && totalFramesInSequence > 0) { // only loop if there's more than one frame, actually
                displayIndex = displayIndex % totalFramesInSequence;
                if (displayIndex < 0) displayIndex += totalFramesInSequence;
            } else {
                displayIndex = Math.max(0, Math.min(displayIndex, totalFramesInSequence - 1));
            }
        }

        int col = displayIndex % numColsInSheet;
        int row = displayIndex / numColsInSheet;

        double x = col * (double) frameWidth;
        double y = row * (double) frameHeight;

        // Validate viewport bounds
        if (x < 0 || y < 0 || x + frameWidth > spriteSheet.getWidth() + 0.001 || y + frameHeight > spriteSheet.getHeight() + 0.001) { // Added tolerance
            System.err.println("SpriteSheetAnimator Warning: Calculated viewport for frame index " + frameIndex +
                               " (effective displayIndex " + displayIndex + " at x:" + x + ", y:" + y + ", w:" + frameWidth + ", h:" + frameHeight +
                               ") is out of sprite sheet bounds (" + spriteSheet.getWidth() + "x" + spriteSheet.getHeight() + "). " +
                               "Check numColsInSheet, totalFramesInSequence, and frame dimensions. Image Path: (Inspect constructor logs)");
            return; // Do not set invalid viewport
        }
        
        imageView.setViewport(new Rectangle2D(x, y, frameWidth, frameHeight));
        this.currentFrameIndex = displayIndex; // Update currentFrameIndex to the one actually set
    }

    public void setOnFinished(Runnable callback) {
        this.onFinishedCallback = callback;
    }

    /**
     * Starts playing the animation.
     * If totalFramesInSequence is 1, it sets the frame and completes (calling onFinished if applicable).
     * Otherwise, it starts an AnimationTimer.
     */
    public void play() {
        if (isPlaying && totalFramesInSequence > 1) { // isPlaying for multi-frame refers to active timer
            System.out.println("SpriteSheetAnimator: Animation (multi-frame) is already playing.");
            return;
        }
        if (spriteSheet == null || spriteSheet.isError()) {
            System.err.println("SpriteSheetAnimator: Cannot play, spriteSheet is null or has an error.");
            return;
        }
         if (fps <= 0 && totalFramesInSequence > 1) { // FPS matters for multi-frame
            System.err.println("SpriteSheetAnimator: FPS must be positive to play multi-frame animation. Animation not started.");
            return;
        }

        // Stop any existing timer before starting a new play session or setting a single frame
        if (animationTimer != null) {
            animationTimer.stop();
        }

        currentFrameIndex = 0; // Always start from the beginning
        setFrame(currentFrameIndex); // Display the first frame immediately

         if (animationTimer != null) animationTimer.stop();

    
    if (totalFramesInSequence == 1) {
        if (this.imageView.getImage() == null || this.imageView.getViewport() == null || currentFrameIndex != 0) {
            // If image/viewport isn't set, or we aren't on frame 0 ensure it's correctly displayed
             setFrame(0); // This ensures it's on frame 0
        }
        this.isPlaying = false;
        // ... onFinished ...
        return;
    }

        // For multi-frame animations:
       animationTimer = new AnimationTimer() {
            @Override
            public void handle(long nowNs) {
                if (lastFrameTimeNs == 0) {
                    lastFrameTimeNs = nowNs;
                    return;
                }

                long elapsedNs = nowNs - lastFrameTimeNs;
                long frameDurationNs = (long) (1_000_000_000.0 / SpriteSheetAnimator.this.fps); // Use outer class fps

                if (elapsedNs >= frameDurationNs) {
                    int nextFrame = currentFrameIndex + 1; // Use a local variable for clarity
                    if (nextFrame >= totalFramesInSequence) {
                        if (loopAnimation) {
                            nextFrame = 0;
                        } else {
                            this.stop(); // Stop this AnimationTimer instance
                            SpriteSheetAnimator.this.isPlaying = false;
                            SpriteSheetAnimator.this.lastFrameTimeNs = 0;
                            if (onFinishedCallback != null) {
                                Platform.runLater(onFinishedCallback);
                            }
                            return; // Animation finished
                        }
                    }
                    setFrame(nextFrame); // setFrame updates currentFrameIndex
                    lastFrameTimeNs = nowNs - (elapsedNs % frameDurationNs);
                }
            }
        };
        lastFrameTimeNs = 0; // Reset for the new play session
        animationTimer.start();
    }

    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        isPlaying = false; // Regardless of whether it was a timer or single frame "playing"
        lastFrameTimeNs = 0;
    }

    public boolean isPlaying() {
        // For multi-frame, isPlaying reflects active timer.
        // For single-frame, play() sets it to false immediately after setting the frame.
        // This getter mostly indicates if an AnimationTimer is currently running.
        return isPlaying;
    }

    
    public void gotoAndStop(int frameIndex) {
        stop(); 

        if (totalFramesInSequence == 0) {
             System.err.println("SpriteSheetAnimator: gotoAndStop - No frames to go to (totalFramesInSequence is 0).");
             return;
        }
        
        setFrame(frameIndex);
    }
}