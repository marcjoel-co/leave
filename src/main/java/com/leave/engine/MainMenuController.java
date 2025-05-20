package com.leave.engine;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Not strictly needed if using direct null checks
import java.util.ResourceBundle;

import static com.leave.engine.utils.AnimationUtils.createBlinkTimeline;
import static com.leave.engine.utils.AnimationUtils.createFadeTransition;
import static com.leave.engine.utils.AnimationUtils.createPauseTransition;
import com.leave.engine.utils.SpriteSheetAnimator;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/*
 * this is the main menu itself where the title game, character selection
 * is shown and displayed
 */

public class MainMenuController implements Initializable {

    //--- FXML Injections: UI Elements from gameEntry.fxml ---
    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundThunderImageView;
    @FXML private StackPane titleGroup;
    @FXML private ImageView logoAnimationImageView;
    @FXML private Label pressKeyLabel;
    @FXML private BorderPane mainMenuGroup;
    @FXML private Label characterNameLabel;
    @FXML private ImageView characterImageView;
    @FXML private Circle backgroundSpotlightCircle;
    @FXML private Button characterChangeButton;
    @FXML private Button newGameButton;
    @FXML private Button loadGameButton;
    @FXML private VBox menuButtonBox;
    @FXML private VBox centerContentVBox;
    @FXML private Label tradeMarc;


    //--- Logo Animation Configuration ---
    private static final String LOGO_SPRITE_SHEET_PATH = "/com/leave/engine/images/LogoIntroAnim.png";
    private static final int LOGO_FRAME_WIDTH = 1080;
    private static final int LOGO_FRAME_HEIGHT = 560;
    private static final int LOGO_NUM_COLS = 32;
    private static final int LOGO_TOTAL_FRAMES = 32;
    private static final double LOGO_FPS = 10.0;
    private SpriteSheetAnimator logoAnimator;
    private boolean logoAnimationFinished = false;
    private boolean skipLogoRequested = false;

    //--- Background (Stormy/Thunder) Animation Configuration ---
    private static final String THUNDER_SPRITE_SHEET_PATH = "/com/leave/engine/images/background/thunder.png";
    private static final int THUNDER_FRAME_WIDTH = 500;
    private static final int THUNDER_FRAME_HEIGHT = 300;
    private static final int THUNDER_NUM_COLS = 5;
    private static final int THUNDER_TOTAL_FRAMES = 5;
    private static final double THUNDER_FPS = 1.0; // Slow FPS for a subtle background
    private SpriteSheetAnimator thunderAnimator;
    private static final double VISIBLE_BACKGROUND_OPACITY = 0.7; // Opacity for stormy background when main menu is visible

    //--- Character Management & UI State ---
    private CharacterManager characterManager;
    private int currentCharIndex = 0;
    private boolean isCharacterAnimating = false;
    // No longer using contentNodewoSpotlight as we explicitly target containers or individual elements in handleCharacterChange

    /*
     * the method for the initializing components of the main menu scene
     * and also the execution or display of graphic interfaces
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("MainMenuController initialize START");

        //conditional statements for critial initialization errors
        if (mainMenuGroup == null || titleGroup == null || rootStackPane == null) {
            System.err.println("CRITICAL: Essential layout panes (mainMenuGroup, titleGroup, rootStackPane) are NULL. FXML linking issue?");
            return;
        }
        if (backgroundSpotlightCircle == null) {
            System.err.println("INFO: backgroundSpotlightCircle is NULL (might be okay if not critical or styled invisible initially).");
        } else {
            System.out.println("SUCCESS: backgroundSpotlightCircle was injected.");
        }

        //Inialized and played animations
        setupAndPlayThunderAnimation();
        setupAndPlayLogoAnimation();

        //Main menu display values
        mainMenuGroup.setOpacity(0.0);
        mainMenuGroup.setMouseTransparent(true);
        titleGroup.setOpacity(1.0);
        titleGroup.setMouseTransparent(false);
    }

    //initializes and plays thunder animation
    private void setupAndPlayThunderAnimation() {
        if (backgroundThunderImageView != null) {
            try {
                thunderAnimator = new SpriteSheetAnimator(
                        backgroundThunderImageView, THUNDER_SPRITE_SHEET_PATH,
                        THUNDER_FRAME_WIDTH, THUNDER_FRAME_HEIGHT,
                        THUNDER_NUM_COLS, THUNDER_TOTAL_FRAMES,
                        THUNDER_FPS, true);
                thunderAnimator.play();
                System.out.println("Background stormy animation started (ImageView initially hidden).");
            } catch (IllegalArgumentException e) {
                System.err.println("Error initializing SpriteSheetAnimator for stormy background: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("backgroundThunderImageView is null. Cannot play stormy animation.");
        }
    }

    private void setupAndPlayLogoAnimation() {
        if (logoAnimationImageView != null) {
            try {
                logoAnimationImageView.setFitWidth(LOGO_FRAME_WIDTH);
                logoAnimationImageView.setFitHeight(LOGO_FRAME_HEIGHT);
                logoAnimationImageView.setPreserveRatio(true);

                logoAnimator = new SpriteSheetAnimator(
                        logoAnimationImageView, LOGO_SPRITE_SHEET_PATH,
                        LOGO_FRAME_WIDTH, LOGO_FRAME_HEIGHT,
                        LOGO_NUM_COLS, LOGO_TOTAL_FRAMES, LOGO_FPS, false);

                logoAnimator.setOnFinished(() -> {
                    logoAnimationFinished = true;
                    if (!skipLogoRequested) {
                        showPressKeyOrTransition();
                    }
                });
                logoAnimator.play();
                System.out.println("Logo animation started.");
            } catch (IllegalArgumentException e) {
                System.err.println("Error initializing SpriteSheetAnimator for logo: " + e.getMessage());
                e.printStackTrace();
                logoAnimationFinished = true;
                transitionToMainMenu();
            }
        } else {
            System.err.println("logoAnimationImageView is null. Cannot play logo animation.");
            logoAnimationFinished = true;
            Platform.runLater(this::setupMainMenuContent);
            Platform.runLater(this::fadeInMainMenuAndBackground);
        }
    }

    public void setupGlobalKeyListener() {
        Scene scene = (rootStackPane != null) ? rootStackPane.getScene() : null;
        if (scene == null && logoAnimationImageView != null) {
            scene = logoAnimationImageView.getScene();
        }
        if (scene != null) {
            scene.setOnKeyPressed(this::handleKeyPressToSkipLogo);
            scene.getRoot().requestFocus();
            System.out.println("Global key listener attached to scene.");
        } else {
            System.err.println("Cannot set up global key listener: Scene is not available.");
        }
    }

    private void handleKeyPressToSkipLogo(KeyEvent event) {
        if (skipLogoRequested || (mainMenuGroup != null && mainMenuGroup.getOpacity() > 0.1)) return;
        skipLogoRequested = true;
        if (logoAnimator != null && logoAnimator.isPlaying()) {
            logoAnimator.stop();
            System.out.println("Logo animation stopped by key press.");
        }
        logoAnimationFinished = true;
        showPressKeyOrTransition();
    }

    private void showPressKeyOrTransition() {
        if (pressKeyLabel == null) {
            transitionToMainMenu();
            return;
        }
        if (skipLogoRequested || logoAnimationFinished) {
            pressKeyLabel.setVisible(true);
            FadeTransition ft = createFadeTransition(pressKeyLabel, Duration.millis(500), 0.0, 1.0);
            ft.setOnFinished(e -> {
                if (skipLogoRequested) transitionToMainMenu();
            });
            ft.play();
            System.out.println("'Press Key' label shown.");
        }
    }

    private void transitionToMainMenu() {
        if (mainMenuGroup != null && mainMenuGroup.getOpacity() > 0.9) return;
        System.out.println("Transitioning to main menu...");
        setupMainMenuContent();

        FadeTransition fadeOutTitle = (titleGroup != null) ? createFadeTransition(titleGroup, Duration.millis(700), 1.0, 0.0) : null;

        if (fadeOutTitle != null) {
            fadeOutTitle.setOnFinished(event -> {
                titleGroup.setMouseTransparent(true);
                if (mainMenuGroup != null) mainMenuGroup.setMouseTransparent(false);
                fadeInMainMenuAndBackground();
            });
            fadeOutTitle.play();
        } else {
            if (mainMenuGroup != null) mainMenuGroup.setMouseTransparent(false);
            fadeInMainMenuAndBackground();
        }
    }

    private void fadeInMainMenuAndBackground() {
        ParallelTransition parallelFadeIn = new ParallelTransition();
        boolean somethingToFade = false;

        if (mainMenuGroup != null) {
            parallelFadeIn.getChildren().add(createFadeTransition(mainMenuGroup, Duration.millis(1000), 0.0, 1.0));
            somethingToFade = true;
        }
        if (backgroundThunderImageView != null) {
            parallelFadeIn.getChildren().add(createFadeTransition(
                backgroundThunderImageView, Duration.millis(1000), 0.0, VISIBLE_BACKGROUND_OPACITY));
            somethingToFade = true;
        }
        
        if (somethingToFade) {
            parallelFadeIn.play();
            parallelFadeIn.setOnFinished(event -> System.out.println("Main menu and background fade-in complete."));
        } else {
            System.err.println("Nothing to fade in for main menu/background.");
        }
    }

    private void setupMainMenuContent() {
        System.out.println("Setting up main menu content (CharacterManager, etc.)");
        List<String> localCharacterImageFiles = List.of(
                "images/characters/1.png", "images/characters/2.png",
                "images/characters/3.png", "images/characters/4.png",
                "images/characters/5.png"
        );
        List<String> localCharacterNames = List.of(
                "Pocholo", "Marc", "Eijel", "Shera", "Denver"
        );

        try {
            if (characterManager == null) { 
                characterManager = new CharacterManager(localCharacterImageFiles, localCharacterNames);
                characterManager.setCurrentIndex(currentCharIndex);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("CRITICAL: Could not initialize CharacterManager. " + e.getMessage());
            if (characterNameLabel != null) characterNameLabel.setText("Character Data Error!");
            if (characterChangeButton != null) characterChangeButton.setDisable(true);
            return;
        }
        setMenuButtonsDisabled(false);
        loadCurrentCharacterDisplay(true); 
        System.out.println("Main menu content setup complete.");
    }

    private void setMenuButtonsDisabled(boolean disabled) {
        if (characterChangeButton != null) characterChangeButton.setDisable(disabled);
        if (newGameButton != null) newGameButton.setDisable(disabled);
        if (loadGameButton != null) loadGameButton.setDisable(disabled);
    }

    private void loadCurrentCharacterDisplay(boolean isInitialLoad) {
        if (characterManager == null) { System.err.println("CharacterManager null in loadDisplay"); return; }
        if (characterImageView == null || characterNameLabel == null) { System.err.println("ImageView or NameLabel null in loadDisplay"); return; }

        String imagePath = characterManager.getCurrentImagePath();
        String charName = characterManager.getCurrentName();

        if (imagePath == null || charName == null) { System.err.println("Null path/name from CharacterManager"); return; }

        Image charImage = null;
        try {
            charImage = new Image(getClass().getResourceAsStream("/com/leave/engine/" + imagePath));
            if (charImage.isError()) {
                System.err.println("Error loading image: " + imagePath + ", " + charImage.getException().getMessage());
                characterImageView.setImage(null);
            } else {
                characterImageView.setImage(charImage);
            }
        } catch (Exception e) {
            System.err.println("Exception loading image: " + imagePath); e.printStackTrace();
            characterImageView.setImage(null);
        }
        characterNameLabel.setText("You are playing as " + charName);

        if (isInitialLoad) {
            if (menuButtonBox != null) menuButtonBox.setOpacity(1.0);
            if (centerContentVBox != null) centerContentVBox.setOpacity(1.0); 
            if (tradeMarc != null) tradeMarc.setOpacity(1.0);
            
            if (backgroundSpotlightCircle != null) backgroundSpotlightCircle.setOpacity(1.0);
            if (characterImageView != null) characterImageView.setOpacity(1.0);
            if (characterNameLabel != null) characterNameLabel.setOpacity(1.0);
        }
    }
    
    @FXML
    private void handleCharacterChange() {
        if (characterManager == null || backgroundThunderImageView == null || backgroundSpotlightCircle == null ||
            characterImageView == null || characterNameLabel == null || menuButtonBox == null || centerContentVBox == null || tradeMarc == null) {
            System.err.println("handleCharacterChange: One or more critical FXML elements for animation are null!");
            return;
        }
        if (isCharacterAnimating || characterManager.getCharacterCount() <= 1) {
            return;
        }
        isCharacterAnimating = true;
        setMenuButtonsDisabled(true);

        // Durations
        Duration spotlightFadeOutDuration = Duration.millis(250);
        Duration mainElementsFadeDuration = Duration.millis(400);
        Duration shortPauseDuration = Duration.millis(100);
        Duration blinkSegmentDuration = Duration.millis(100);
        Duration revealPauseDuration = Duration.millis(150);

        // --- FADE OUT SEQUENCE ---
        // 1. Spotlight fades out FIRST
        FadeTransition spotlightFadeOut = createFadeTransition(
                backgroundSpotlightCircle, spotlightFadeOutDuration, backgroundSpotlightCircle.getOpacity(), 0.0);

        // 2. THEN, after spotlight is out, fade out everything else in parallel
        ParallelTransition allOtherElementsFadeOut = new ParallelTransition();
        allOtherElementsFadeOut.getChildren().add(createFadeTransition(backgroundThunderImageView, mainElementsFadeDuration, backgroundThunderImageView.getOpacity(), 0.0));
        allOtherElementsFadeOut.getChildren().add(createFadeTransition(menuButtonBox, mainElementsFadeDuration, menuButtonBox.getOpacity(), 0.0));
        allOtherElementsFadeOut.getChildren().add(createFadeTransition(centerContentVBox, mainElementsFadeDuration, centerContentVBox.getOpacity(), 0.0)); // Fades char name, image, btn inside
        allOtherElementsFadeOut.getChildren().add(createFadeTransition(tradeMarc, mainElementsFadeDuration, tradeMarc.getOpacity(), 0.0));
        // Note: characterImageView and characterNameLabel are part of centerContentVBox, so they'll fade with it.

        SequentialTransition fullFadeOutToBlack = new SequentialTransition(
            spotlightFadeOut,
            createPauseTransition(shortPauseDuration),
            allOtherElementsFadeOut,
            createPauseTransition(shortPauseDuration)
        );

        fullFadeOutToBlack.setOnFinished(fadeOutEvent -> {
            characterManager.nextCharacter();
            
            if(characterImageView != null) characterImageView.setOpacity(0.0);
            if(characterNameLabel != null) { characterNameLabel.setOpacity(0.0); characterNameLabel.setText(""); }
            if(backgroundSpotlightCircle != null) backgroundSpotlightCircle.setOpacity(0.0);
            

            loadCurrentCharacterDisplay(false); // Loads new data into (currently opacity 0) views

            // --- FADE IN / BLINK IN EVERYTHING ---
            ParallelTransition allElementsFadeIn = new ParallelTransition();

            allElementsFadeIn.getChildren().add(createFadeTransition(backgroundThunderImageView, mainElementsFadeDuration, 0.0, VISIBLE_BACKGROUND_OPACITY));
            allElementsFadeIn.getChildren().add(createBlinkTimeline(backgroundSpotlightCircle, blinkSegmentDuration, 1.0));
            allElementsFadeIn.getChildren().add(createBlinkTimeline(menuButtonBox, blinkSegmentDuration.add(Duration.millis(20)), 1.0));
            allElementsFadeIn.getChildren().add(createBlinkTimeline(centerContentVBox, blinkSegmentDuration.add(Duration.millis(20)), 1.0)); // Blinks in char image, name, button
            allElementsFadeIn.getChildren().add(createBlinkTimeline(tradeMarc, blinkSegmentDuration.add(Duration.millis(20)), 1.0));
            
            // Re-blinking character image and name for explicit control, though centerContentVBox blink might cover it
            allElementsFadeIn.getChildren().add(createBlinkTimeline(characterImageView, blinkSegmentDuration, 1.0));
            allElementsFadeIn.getChildren().add(createFadeTransition(characterNameLabel, blinkSegmentDuration.multiply(5), 0.0, 1.0));


            SequentialTransition showEverythingSequence = new SequentialTransition(
                createPauseTransition(revealPauseDuration),
                allElementsFadeIn
            );

            showEverythingSequence.play();
            showEverythingSequence.setOnFinished(fadeInEvent -> {
                isCharacterAnimating = false;
                setMenuButtonsDisabled(false);
                System.out.println("Character change animation finished with new sequence.");
            });
        });

        fullFadeOutToBlack.play();
        System.out.println("Character change animation sequence (spotlight first) started.");
    }

    @FXML
    public void handleNewGame(ActionEvent event) {
        System.out.println("handleNewGame in MainMenuController called! Transitioning to dialogue...");
        try {
            if (thunderAnimator != null) thunderAnimator.stop();
            if (logoAnimator != null) logoAnimator.stop();
            App.setRoot("secondary"); 
        } catch (IOException e) {
            System.err.println("Error loading secondary.fxml for new game:");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLoadGame(ActionEvent event) {
        System.out.println("handleLoadGame (Quit) in MainMenuController called!");
        if (thunderAnimator != null) thunderAnimator.stop();
        if (logoAnimator != null) logoAnimator.stop();
        javafx.application.Platform.exit();
    }
}