package com.leave.engine;

import java.net.URL;
import java.util.ResourceBundle;

import com.leave.engine.utils.SpriteSheetAnimator;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

//A Title panel class that implements to Initializable
public class TitlePanel implements Initializable {

    // Imageview Varibale for logo animation
    @FXML
    private ImageView logoAnimationImageView;

    //file path of the png animation
    private static final String SPRITE_SHEET_PATH = "/com/leave/engine/images/LogoIntroAnim.png"; 

    //width and hight values of the png
    private static final int SPRITE_SHEET_TOTAL_WIDTH = 35460;
    private static final int SPRITE_SHEET_TOTAL_HEIGHT = 560;

    //variable values for how the logo animation will play
    private static final int TOTAL_FRAMES = 32;
    private static final int NUM_COLS = TOTAL_FRAMES; 
    private static final int NUM_ROWS = 1;          

    //variable values for how big is the image will display
    private static final int FRAME_IMAGE_WIDTH = 1080;
    private static final int FRAME_IMAGE_HEIGHT = SPRITE_SHEET_TOTAL_HEIGHT / NUM_ROWS; 

    //value for how many frame will it show per second
    private static final int FPS = 10; 

    //values for how big will the animation will show
    private static final double DISPLAY_FRAME_WIDTH = FRAME_IMAGE_WIDTH;   
    private static final double DISPLAY_FRAME_HEIGHT = FRAME_IMAGE_HEIGHT; 

    //A variable for animating spritesheets
    private SpriteSheetAnimator animator;
    private Runnable onAnimationFinishedCallback;

    //A method that tells if the animation is finished
    public void setOnAnimationFinished(Runnable callback) {
        this.onAnimationFinishedCallback = callback;
        if (animator != null) {
            animator.setOnFinished(callback);
        }
    }

    //A method to initialize said animation
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        // 

        // test if integer
        if (SPRITE_SHEET_TOTAL_WIDTH % NUM_COLS != 0) {
            System.out.println("WARNING: Sprite sheet total width (" + SPRITE_SHEET_TOTAL_WIDTH +
                               ") is not perfectly divisible by the number of columns/frames (" + NUM_COLS +
                               "). Calculated frame width is " + FRAME_IMAGE_WIDTH +
                               ". Animation might appear slightly cut off at the end.");
        }

        try {
            // Set the ImageView to the size of one frame
            logoAnimationImageView.setFitWidth(DISPLAY_FRAME_WIDTH);
            logoAnimationImageView.setFitHeight(DISPLAY_FRAME_HEIGHT);
            // Preserve ratio is good, though if fitWidth/fitHeight match frame aspect ratio, it's less critical.
            // Setting it to true ensures no distortion if, for some reason,
            // DISPLAY_FRAME_WIDTH/HEIGHT didn't perfectly match FRAME_IMAGE_WIDTH/HEIGHT aspect ratio.
            logoAnimationImageView.setPreserveRatio(true);

            //the values of which what is being animated
            animator = new SpriteSheetAnimator(
                    logoAnimationImageView,
                    SPRITE_SHEET_PATH,
                    FRAME_IMAGE_WIDTH,    // 1108
                    FRAME_IMAGE_HEIGHT,   // 560
                    NUM_COLS,         
                    TOTAL_FRAMES,     
                    FPS,false
            );
            
            //if animationcallback is not set
            if (this.onAnimationFinishedCallback != null) {
                animator.setOnFinished(this.onAnimationFinishedCallback);
            }

        } catch (IllegalArgumentException e) {
            System.err.println("Error initializing TitlePanel's SpriteSheetAnimator: " + e.getMessage());
            e.printStackTrace();
            if (logoAnimationImageView != null) {
                logoAnimationImageView.setImage(null); // Clear on error
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in TitlePanel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //A method that plays the initialized spritesheet
    public void playAnimation() {

        //conditional statements if logo played or not
        if (animator != null) {
            System.out.println("TitlePanel: Playing animation. ImageView size: " +
                               logoAnimationImageView.getFitWidth() + "x" + logoAnimationImageView.getFitHeight() +
                               ". Frame size for animator: " + FRAME_IMAGE_WIDTH + "x" + FRAME_IMAGE_HEIGHT);
            animator.play();
        } else {
            System.err.println("Animator not initialized, cannot play animation.");
            if (onAnimationFinishedCallback != null) {
                onAnimationFinishedCallback.run();
            }
        }
        
    }

    //A method to stop the animation when finished
    public void stopAnimation() {
        if (animator != null) {
            animator.stop();
        }
    }
}