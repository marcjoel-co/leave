package com.leave.engine.utils;

import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

public class SpriteSheetAnimator {

    private final ImageView imageView;
    private final Image spriteSheet;
    private final int frameWidth;
    private final int frameHeight;
    private final int numCols;
    private final int totalFrames;
    private final double fps;

    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private AnimationTimer animationTimer;
    private Runnable onFinishedCallback; 
    private boolean isPlaying = false;

    /**
     * Constructor for the SpriteSheetAnimator.
     *
     * @param imageView   The ImageView to display the animation
     * @param spriteSheetPath Path to the spritesheet image in resources (e.g., "/images/sheet.png").
     * @param frameWidth  Width of a single frame.
     * @param frameHeight Height of a single frame.
     * @param numCols     Number of columns in the spritesheet.
     * @param totalFrames Total number of frames in the animation.
     * @param fps         Desired frames per second.
     * @throws IllegalArgumentException if the spritesheet cannot be loaded.
     */
    public SpriteSheetAnimator(ImageView imageView, String spriteSheetPath,
                               int frameWidth, int frameHeight, int numCols,
                               int totalFrames, double fps) {
        if (imageView == null) {
            throw new IllegalArgumentException("ImageView cannot be null.");
        }
        this.imageView = imageView;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.numCols = numCols;
        this.totalFrames = totalFrames;
        this.fps = fps;

        URL spriteUrl = getClass().getResource(spriteSheetPath);
        if (spriteUrl == null) {
            throw new IllegalArgumentException("Sprite sheet not found at: " + spriteSheetPath);
        }
        this.spriteSheet = new Image(spriteUrl.toExternalForm());
        if (this.spriteSheet.isError()) {
            throw new IllegalArgumentException("Error loading sprite sheet: " + spriteSheetPath, this.spriteSheet.getException());
        }

        this.imageView.setImage(this.spriteSheet);
        // for good 
        // The caller can do this, or we can enforce it.
        // this.imageView.setFitWidth(frameWidth);
        // this.imageView.setFitHeight(frameHeight);
        setFrame(0); // Display the first frame initially
    }

   // In SpriteSheetAnimator.java
// private final ImageView imageView; // Given
// private final Image spriteSheet;   // Loaded from path
// private final int frameWidth;      // From TitlePanel: 1108
// private final int frameHeight;     // From TitlePanel: 560
// private final int numCols;         // From TitlePanel: 32 (total frames for a horizontal strip)
// // private final int totalFrames;  // From TitlePanel: 32


// ...

private void setFrame(int frameIndex) {
    if (spriteSheet == null || spriteSheet.isError() || imageView == null) return;

    // For a horizontal strip (numRows = 1 implicitly or explicitly):
    int col = frameIndex % numCols; // This should give 0, 1, 2, ..., 31 for frameIndex 0-31 if numCols = 32
    int row = frameIndex / numCols; // This should always be 0 if frameIndex < numCols

    double x = col * frameWidth;    // This is the crucial calculation for horizontal position
    double y = row * frameHeight;   // This should always be 0 * 560 = 0

    // The logging you added here previously would be invaluable now!
    System.out.println("SpriteSheetAnimator: Setting frame " + frameIndex +
                       ". Viewport: x=" + x + ", y=" + y + ", w=" + frameWidth + ", h=" + frameHeight +
                       " (col=" + col + ", row=" + row + ")");

    imageView.setViewport(new Rectangle2D(x, y, frameWidth, frameHeight));

    imageView.setCache(false); // Try disabling cache temporarily
    imageView.setClip(null); // Ensure no rogue clip
    imageView.getTransforms().clear(); // Ensure no rogue transforms
// If it has a parent, maybe force parent layout
    if (imageView.getParent() != null) {
        imageView.getParent().requestLayout();
}

}

    public void setOnFinished(Runnable callback) {
        this.onFinishedCallback = callback;
    }

    public void play() {
        if (isPlaying) {
            return; // Already playing
        }
        if (animationTimer != null) {
            animationTimer.stop();
        }
        currentFrame = 0;
        setFrame(currentFrame); // Ensure first frame is shown
        isPlaying = true;

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameTime == 0) { // Initialize for the first frame
                    lastFrameTime = now;
                    return;
                }

                if (now - lastFrameTime >= (1_000_000_000.0 / fps)) {
                    currentFrame++;
                    if (currentFrame >= totalFrames) {
                        this.stop();
                        isPlaying = false;
                        lastFrameTime = 0; // Reset for next play
                        if (onFinishedCallback != null) {
                            onFinishedCallback.run();
                        }
                        return;
                    }
                    setFrame(currentFrame);
                    lastFrameTime = now;
                }
            }
        };
        lastFrameTime = 0; 
        animationTimer.start();
    }

    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        isPlaying = false;
        lastFrameTime = 0; 
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    // Optional: Method to jump to a specific frame
    public void gotoAndStop(int frameIndex) {
        stop();
        if (frameIndex >= 0 && frameIndex < totalFrames) {
            currentFrame = frameIndex;
            setFrame(currentFrame);
        } else {
            System.err.println("Frame index out of bounds: " + frameIndex);
        }
    }

    

}