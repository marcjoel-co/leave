package com.leave.engine; // Or your actual package

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MinimalTest extends Application {

    // Ensure these constants match your setup and sprite sheet
    // This path MUST be correct from the root of your resources folder
    private static final String SPRITE_SHEET_PATH = "/com/leave/engine/images/LogoIntroAnim.png";
    private static final int FRAME_WIDTH = 1108;
    private static final int FRAME_HEIGHT = 560;
    // numCols isn't strictly needed here if we directly calculate x for frame 1
    // but good to have for context.

    @Override
    public void start(Stage primaryStage) {
        ImageView imageView = new ImageView();
        Image spriteSheet;

        System.out.println("MinimalTest: Attempting to load image: " + SPRITE_SHEET_PATH);
        try {
            // Make sure the image is in src/main/resources/com/leave/engine/images/LogoIntroAnim.png
            // Using getResourceAsStream is generally robust.
            spriteSheet = new Image(getClass().getResourceAsStream(SPRITE_SHEET_PATH));
            if (spriteSheet.isError()) {
                System.err.println("MinimalTest: Error loading image. Path: " + SPRITE_SHEET_PATH);
                System.err.println("Error details: " + spriteSheet.getException().getMessage());
                spriteSheet.getException().printStackTrace();
                return; // Exit if image fails to load
            }
            imageView.setImage(spriteSheet);
            System.out.println("MinimalTest: Image loaded successfully. Width: " + spriteSheet.getWidth() + ", Height: " + spriteSheet.getHeight());
        } catch (NullPointerException e) {
            // This typically happens if getResourceAsStream returns null (path issue)
            System.err.println("MinimalTest: NullPointerException loading image. Is the path correct? Path: " + SPRITE_SHEET_PATH);
            e.printStackTrace();
            return;
        } catch (Exception e) {
            System.err.println("MinimalTest: Generic exception loading image. Path: " + SPRITE_SHEET_PATH);
            e.printStackTrace();
            return;
        }

        // === Test Sequence ===

        // 1. Display Frame 0 (x=0)
        double frame0_x = 0 * FRAME_WIDTH; // Should be 0
        double frame0_y = 0 * FRAME_HEIGHT; // Should be 0
        System.out.println("MinimalTest: Setting viewport for Frame 0: x=" + frame0_x + ", y=" + frame0_y + ", w=" + FRAME_WIDTH + ", h=" + FRAME_HEIGHT);
        imageView.setViewport(new Rectangle2D(frame0_x, frame0_y, FRAME_WIDTH, FRAME_HEIGHT));

        // 2. Pause, then display Frame 1 (x=FRAME_WIDTH)
        PauseTransition pause1 = new PauseTransition(Duration.seconds(3));
        pause1.setOnFinished(event -> {
            double frame1_x = 1 * FRAME_WIDTH; // Should be 1108
            double frame1_y = 0 * FRAME_HEIGHT; // Should be 0
            System.out.println("MinimalTest: Setting viewport for Frame 1: x=" + frame1_x + ", y=" + frame1_y + ", w=" + FRAME_WIDTH + ", h=" + FRAME_HEIGHT);
            if (spriteSheet.isError()) { // Re-check image just in case
                System.err.println("MinimalTest: Image developed an error before setting Frame 1.");
                return;
            }
            imageView.setViewport(new Rectangle2D(frame1_x, frame1_y, FRAME_WIDTH, FRAME_HEIGHT));

            // 3. Pause, then display Frame 0 again (x=0)
            PauseTransition pause2 = new PauseTransition(Duration.seconds(3));
            pause2.setOnFinished(event2 -> {
                double frame0_again_x = 0 * FRAME_WIDTH;
                double frame0_again_y = 0 * FRAME_HEIGHT;
                System.out.println("MinimalTest: Setting viewport for Frame 0 AGAIN: x=" + frame0_again_x + ", y=" + frame0_again_y + ", w=" + FRAME_WIDTH + ", h=" + FRAME_HEIGHT);
                if (spriteSheet.isError()) {
                     System.err.println("MinimalTest: Image developed an error before setting Frame 0 again.");
                     return;
                }
                imageView.setViewport(new Rectangle2D(frame0_again_x, frame0_again_y, FRAME_WIDTH, FRAME_HEIGHT));
                System.out.println("MinimalTest: Test sequence complete.");
            });
            pause2.play();
        });
        pause1.play();


        // Setup the scene and stage
        StackPane root = new StackPane(imageView);
        // Use a distinct background to easily see the ImageView's bounds and if it's transparent.
        root.setStyle("-fx-background-color: cornflowerblue;"); // Bright, distinct color

        // Scene size a bit larger than one frame to see it clearly
        Scene scene = new Scene(root, FRAME_WIDTH + 200, FRAME_HEIGHT + 200);

        primaryStage.setTitle("Minimal Sprite Viewport Test");
        primaryStage.setScene(scene);
        // To make it easier to see, don't run this one full screen initially.
        // primaryStage.setFullScreen(true);
        primaryStage.show();

        System.out.println("MinimalTest: Stage is shown.");
    }

    public static void main(String[] args) {
        // This is how you launch a JavaFX application
        launch(args);
    }
}