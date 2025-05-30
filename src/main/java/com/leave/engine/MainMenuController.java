package com.leave.engine;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.leave.engine.utils.AnimationUtils.createBlinkTimeline;
import static com.leave.engine.utils.AnimationUtils.createFadeTransition;
import static com.leave.engine.utils.AnimationUtils.createPauseTransition;
import com.leave.engine.utils.SpriteSheetAnimator;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Import Alert
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

public class MainMenuController implements Initializable {

    //--- FXML Injections ---
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
    @FXML private Button loadGameButton; // Currently Quit
    @FXML private VBox menuButtonBox;
    @FXML private VBox centerContentVBox;
    @FXML private Label tradeMarc;

    //logo
    private static final String LOGO_SPRITE_SHEET_PATH = "/com/leave/engine/images/LogoIntroAnim.png";
    private static final int LOGO_FRAME_WIDTH = 1080;
    private static final int LOGO_FRAME_HEIGHT = 560;
    private static final int LOGO_NUM_COLS = 32;
    private static final int LOGO_TOTAL_FRAMES = 32;
    private static final double LOGO_FPS = 10.0;
    private SpriteSheetAnimator logoAnimator;
    private volatile boolean logoAnimationFinished = false; // Volatile for thread visibility
    private volatile boolean skipLogoRequested = false;     // Volatile

    //thunder config
    private static final String THUNDER_SPRITE_SHEET_PATH = "/com/leave/engine/images/backgrounds/thunder.png";
    // ... (thunder animation constants) ...
    private static final int THUNDER_FRAME_WIDTH = 500;
    private static final int THUNDER_FRAME_HEIGHT = 300;
    private static final int THUNDER_NUM_COLS = 5;
    private static final int THUNDER_TOTAL_FRAMES = 5;
    private static final double THUNDER_FPS = 1.0;
    private SpriteSheetAnimator thunderAnimator;
    private static final double VISIBLE_BACKGROUND_OPACITY = 0.7;


    //--- Character Selection Logic ---
    private CharacterManager characterManager;
    private int currentCharIndex = 0;
    private boolean isCharacterAnimating = false;
    private volatile boolean mainMenuContentIsSetup = false; // Flag to ensure CharacterManager is ready

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("MainMenuController: initialize START");
        if (mainMenuGroup == null || titleGroup == null || rootStackPane == null) {
            System.err.println("MainMenuController CRITICAL: Essential layout panes (mainMenuGroup, titleGroup, rootStackPane) are NULL. FXML linking issue?");
            return;
        }
        // ... other FXML element null checks for robustness (optional but good)

        
        ensureMainMenuContentIsSetup();

        
        if (mainMenuGroup != null) {
            mainMenuGroup.setOpacity(0.0);
            mainMenuGroup.setMouseTransparent(true); // Prevent clicks
        }
        if (titleGroup != null) { 
            titleGroup.setOpacity(1.0);
            titleGroup.setMouseTransparent(false); // Can be clicked/interacted with if needed for skip
        }

        // Disable menu buttons until main menu is fully set up and displayed
        setMenuButtonsDisabled(true); // Explicitly disable at start

        setupAndPlayThunderAnimation();
        setupAndPlayLogoAnimation();
        System.out.println("MainMenuController: initialize FINISHED");
    }

    private synchronized void ensureMainMenuContentIsSetup() {
        if (!mainMenuContentIsSetup) {
            setupMainMenuContent();
        }
    }

    private void setupAndPlayThunderAnimation() {
        
        if (backgroundThunderImageView != null) {
            try {
                thunderAnimator = new SpriteSheetAnimator(
                        backgroundThunderImageView, THUNDER_SPRITE_SHEET_PATH,
                        THUNDER_FRAME_WIDTH, THUNDER_FRAME_HEIGHT,
                        THUNDER_NUM_COLS, THUNDER_TOTAL_FRAMES, THUNDER_FPS, true);
                thunderAnimator.play();
                 System.out.println("MainMenuController: Thunder animation started.");
            } catch (IllegalArgumentException e) {
                System.err.println("MainMenuController Error: initializing SpriteSheetAnimator for thunder: " + e.getMessage());
            }
        } else {
            System.err.println("MainMenuController Error: backgroundThunderImageView is null. Cannot play thunder animation.");
        }
    }

    private void setupAndPlayLogoAnimation() {
        System.out.println("MainMenuController: setupAndPlayLogoAnimation START");
        if (logoAnimationImageView != null) {
            try {
                logoAnimationImageView.setFitWidth(LOGO_FRAME_WIDTH);
                logoAnimationImageView.setFitHeight(LOGO_FRAME_HEIGHT);
                logoAnimationImageView.setPreserveRatio(true);

                logoAnimator = new SpriteSheetAnimator(
                        logoAnimationImageView, LOGO_SPRITE_SHEET_PATH,
                        LOGO_FRAME_WIDTH, LOGO_FRAME_HEIGHT,
                        LOGO_NUM_COLS, LOGO_TOTAL_FRAMES, LOGO_FPS, false); // loop=false
                System.out.println("MainMenuController: SpriteSheetAnimator for logo created with TotalFrames=" + LOGO_TOTAL_FRAMES);

                logoAnimator.setOnFinished(() -> {
                    Platform.runLater(() -> { // Ensure UI updates on JavaFX Application Thread
                        System.out.println("MainMenuController: Logo animation ON_FINISHED callback.");
                        logoAnimationFinished = true;
                        if (!skipLogoRequested) {
                            System.out.println("MainMenuController: Logo finished naturally, calling showPressKeyOrTransition.");
                            showPressKeyOrTransition();
                        } else {
                            System.out.println("MainMenuController: Logo finished but was already skipped; transitionToMainMenu should have occurred.");
                            // Safety check: if somehow menu isn't up, trigger transition.
                            if (mainMenuGroup != null && mainMenuGroup.getOpacity() < 0.1) {
                                transitionToMainMenu();
                            }
                        }
                    });
                });
                logoAnimator.play();
                System.out.println("MainMenuController: Logo animation play() called.");
            } catch (IllegalArgumentException e) {
                System.err.println("MainMenuController Error: initializing SpriteSheetAnimator for logo: " + e.getMessage());
                logoAnimationFinished = true; // Still mark as finished to proceed
                skipLogoRequested = true;     // Treat as skipped
                transitionToMainMenu();       // Try to go directly to main menu
            }
        } else {
            System.err.println("MainMenuController Error: logoAnimationImageView is null. Skipping logo animation.");
            logoAnimationFinished = true;
            skipLogoRequested = true;
            transitionToMainMenu(); // Go directly to main menu if no logo view
        }
    }

    public void setupGlobalKeyListener() {
        // Defer getting scene until it's more likely available, or App ensures it.
        // This is often called by App after stage.show()
        Platform.runLater(() -> { // Ensure it runs after scene might be ready
            Scene scene = (rootStackPane != null) ? rootStackPane.getScene() : null;
            if (scene == null && logoAnimationImageView != null) scene = logoAnimationImageView.getScene(); // Fallback
            
            if (scene != null) {
                scene.setOnKeyPressed(this::handleKeyPressToSkipLogo);
                System.out.println("MainMenuController: Global key listener for logo skip ATTACHED to scene.");
            } else {
                System.err.println("MainMenuController Warning: Cannot set up global key listener for logo skip: Scene is not available yet even after Platform.runLater.");
                
            }
        });
    }

    private void handleKeyPressToSkipLogo(KeyEvent event) {
        System.out.println("MainMenuController: handleKeyPressToSkipLogo - Key: " + event.getCode());
        if (mainMenuGroup != null && mainMenuGroup.getOpacity() > 0.1 && !mainMenuGroup.isMouseTransparent()) {
            System.out.println("MainMenuController: handleKeyPressToSkipLogo - Main menu already visible and interactive. Ignoring further skips.");
            return; // Main menu is already up and interactive, don't re-trigger.
        }

        if (skipLogoRequested && logoAnimationFinished) {
            // If logo was already marked as skipped AND finished (e.g. error path),
            // this key press might be for interacting with the 'Press Key' label, or for menu itself.
            // If "Press Key" label is visible, this press should make it transition.
            if (pressKeyLabel != null && pressKeyLabel.isVisible()) {
                System.out.println("MainMenuController: handleKeyPressToSkipLogo - 'Press Key' label visible. Transitioning to main menu.");
                transitionToMainMenu(); // Transition now based on key press on "Press Key" label.
            }
            return; // Avoid reprocessing if already dealt with.
        }
        
        if (skipLogoRequested) return; // Already processing a skip

        skipLogoRequested = true;
        logoAnimationFinished = true; // Mark as finished as we are skipping

        if (logoAnimator != null && logoAnimator.isPlaying()) {
            System.out.println("MainMenuController: handleKeyPressToSkipLogo - Stopping logo animator.");
            logoAnimator.stop(); // Stop the visual animation
        }

        // Hide "Press Key" label if it was shown.
        if (pressKeyLabel != null) pressKeyLabel.setVisible(false);

        System.out.println("MainMenuController: handleKeyPressToSkipLogo - Logo skip requested. Transitioning to main menu.");
        transitionToMainMenu(); // Skip logo and "Press Key" phase, go directly to menu.
    }

    private void showPressKeyOrTransition() {
        System.out.println("MainMenuController: showPressKeyOrTransition - skipLogoRequested=" + skipLogoRequested + ", logoAnimationFinished=" + logoAnimationFinished);

        if (skipLogoRequested) { // If already skipped by a key press, transitionToMainMenu was called.
            System.out.println("MainMenuController: showPressKeyOrTransition - Logo already skipped. Transition should be in progress.");
            return;
        }

        if (pressKeyLabel == null) {
            System.err.println("MainMenuController: pressKeyLabel is null. Attempting direct transitionToMainMenu.");
            transitionToMainMenu(); // Should not happen if FXML is correct
            return;
        }

        if (logoAnimationFinished) { // Only if logo finished naturally (not skipped before this point)
            System.out.println("MainMenuController: showPressKeyOrTransition - Logo finished naturally. Showing 'Press Key' label.");
            pressKeyLabel.setVisible(true);
            FadeTransition ft = createFadeTransition(pressKeyLabel, Duration.millis(500), 0.0, 1.0);
            // The key press will be handled by the global setOnKeyPressed via handleKeyPressToSkipLogo.
            // That method will now see that logoAnimationFinished=true, skipLogoRequested=false initially,
            // and then proceed to call transitionToMainMenu.
            ft.play();
        }
    }

    private synchronized void transitionToMainMenu() {
        if (mainMenuGroup != null && mainMenuGroup.getOpacity() > 0.9 && !mainMenuGroup.isMouseTransparent()) {
            System.out.println("MainMenuController: transitionToMainMenu - Main menu already fully visible and interactive. Skipping.");
            return;
        }
        System.out.println("MainMenuController: transitionToMainMenu - Attempting to transition.");

        ensureMainMenuContentIsSetup(); // CRITICAL: Ensure CharacterManager is ready
        if (!mainMenuContentIsSetup) {
             System.err.println("MainMenuController CRITICAL in transitionToMainMenu: mainMenuContentIsSetup is still false after ensure! Cannot proceed to show menu.");
             // Potentially show an error alert.
            return;
        }

        // Proceed with UI transition
        // Stop pressKeyLabel interactions if it was part of the flow
        if (pressKeyLabel != null) {
            pressKeyLabel.setOpacity(0.0); // Hide it quickly
            pressKeyLabel.setVisible(false);
        }


        FadeTransition fadeOutTitle = null;
        if (titleGroup != null && titleGroup.getOpacity() > 0.0) { // Only fade if visible
            fadeOutTitle = createFadeTransition(titleGroup, Duration.millis(700), titleGroup.getOpacity(), 0.0);
        }

        Runnable showMainMenuRunnable = () -> {
            if (titleGroup != null) titleGroup.setMouseTransparent(true);
            if (mainMenuGroup != null) {
                System.out.println("MainMenuController: Making mainMenuGroup mouse-transparent false (interactive).");
                mainMenuGroup.setMouseTransparent(false); // MAKE MENU INTERACTIVE
                setMenuButtonsDisabled(false);          // And enable buttons
            }
            fadeInMainMenuAndBackground();
        };

        if (fadeOutTitle != null) {
            fadeOutTitle.setOnFinished(event -> showMainMenuRunnable.run());
            fadeOutTitle.play();
        } else {
            showMainMenuRunnable.run(); // No title fade, proceed directly
        }
    }

    private void fadeInMainMenuAndBackground() {
        // ... (your existing fade in logic, should be fine)
        System.out.println("MainMenuController: fadeInMainMenuAndBackground called.");
        ParallelTransition parallelFadeIn = new ParallelTransition();
        boolean somethingToFade = false;

        if (mainMenuGroup != null && mainMenuGroup.getOpacity() < 1.0) {
            parallelFadeIn.getChildren().add(createFadeTransition(mainMenuGroup, Duration.millis(1000), mainMenuGroup.getOpacity(), 1.0));
            somethingToFade = true;
        }
        if (backgroundThunderImageView != null && backgroundThunderImageView.getOpacity() < VISIBLE_BACKGROUND_OPACITY) {
            parallelFadeIn.getChildren().add(createFadeTransition(
                backgroundThunderImageView, Duration.millis(1000), backgroundThunderImageView.getOpacity(), VISIBLE_BACKGROUND_OPACITY));
            somethingToFade = true;
        }
        
        if (somethingToFade) {
            parallelFadeIn.play();
            parallelFadeIn.setOnFinished(event -> System.out.println("MainMenuController: Main menu and background fade-in complete."));
        } else {
            System.out.println("MainMenuController: fadeInMainMenuAndBackground - Nothing to fade in (already at target opacity).");
        }
    }


    private synchronized void setupMainMenuContent() {
        if (mainMenuContentIsSetup) {
            System.out.println("MainMenuController: setupMainMenuContent - Already setup.");
            return; // Prevent re-initialization
        }
        System.out.println("MainMenuController: setupMainMenuContent STARTING.");
        
        List<String> localCharacterMenuImageFiles = List.of(
                "images/characters/Hera.png", "images/characters/norman.png",
                "images/characters/Jewel.png", "images/characters/Batman.png"
        );
        List<String> localCharacterNames = List.of("Hera", "Norman", "Jewel", "batman");

        try {
            characterManager = new CharacterManager(localCharacterMenuImageFiles, localCharacterNames);
            characterManager.setCurrentIndex(currentCharIndex);
            mainMenuContentIsSetup = true; // Set flag ON SUCCESS
            System.out.println("MainMenuController: CharacterManager INITIALIZED successfully. mainMenuContentIsSetup = true.");
        } catch (IllegalArgumentException e) {
            System.err.println("MainMenuController CRITICAL: Could not initialize CharacterManager: " + e.getMessage());
            mainMenuContentIsSetup = false; // Explicitly false on error
            if (characterNameLabel != null) characterNameLabel.setText("Char Data Err!");
            if (characterChangeButton != null) characterChangeButton.setDisable(true);
            if (newGameButton != null) newGameButton.setDisable(true);
            return;
        }
        // Don't enable buttons here, transitionToMainMenu will enable them when group is interactive
        // setMenuButtonsDisabled(false); 
        loadCurrentCharacterDisplay(true); // Load the first character for display
        System.out.println("MainMenuController: setupMainMenuContent FINISHED.");
    }

    private void setMenuButtonsDisabled(boolean disabled) {
        System.out.println("MainMenuController: setMenuButtonsDisabled to " + disabled);
        if (characterChangeButton != null) characterChangeButton.setDisable(disabled);
        if (newGameButton != null) newGameButton.setDisable(disabled);
        if (loadGameButton != null) loadGameButton.setDisable(disabled); // This is "Quit"
    }

    private void loadCurrentCharacterDisplay(boolean isInitialLoad) {
        // ... (your existing loadCurrentCharacterDisplay, should be fine) ...
        // Make sure it gracefully handles characterManager == null just in case, though ensureMainMenuContentIsSetup should prevent that.
        if (characterManager == null || characterImageView == null || characterNameLabel == null) {
            System.err.println("MainMenuController: Cannot load character display: CharacterManager or FXML elements are null.");
            return;
        }

        String relativeImagePath = characterManager.getCurrentImagePath(); 
        String charName = characterManager.getCurrentName();              

        if (relativeImagePath == null || charName == null) {
            System.err.println("MainMenuController: Null image path or name from CharacterManager during load display.");
            return;
        }
        
        String fullResourcePath = "/com/leave/engine/" + relativeImagePath;
        try {
            Image charImage = new Image(getClass().getResourceAsStream(fullResourcePath));
            if (charImage.isError()) {
                System.err.println("MainMenuController Error loading menu character image: " + fullResourcePath + ", " + charImage.getException().getMessage());
                characterImageView.setImage(null);
            } else {
                characterImageView.setImage(charImage);
            }
        } catch (Exception e) {
            System.err.println("MainMenuController Exception loading menu character image: " + fullResourcePath);
            e.printStackTrace();
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
        ensureMainMenuContentIsSetup(); // Ensure manager is ready
        if (!mainMenuContentIsSetup || characterManager == null) {
             System.err.println("MainMenuController: handleCharacterChange - Cannot change character, content not setup."); return;
        }
        // ... (Your existing complex animation logic - assume it's okay if it worked before) ...
        // The logic to disable buttons, animate, call characterManager.nextCharacter(),
        // then loadCurrentCharacterDisplay(false), and re-enable buttons.
        if (isCharacterAnimating || characterManager.getCharacterCount() <= 1) return;
        
        // ... (rest of your animation logic)
        // Simplified structure - ensure key parts are there
        isCharacterAnimating = true;
        setMenuButtonsDisabled(true);
        // Animations ...
        // on animation finish:
            // characterManager.nextCharacter();
            // loadCurrentCharacterDisplay(false);
            // isCharacterAnimating = false;
            // setMenuButtonsDisabled(false);
        // For brevity, assuming your existing detailed animation transitions in here are fine
        // Your existing `handleCharacterChange` was complex; ensure the core flow:
        // 1. Start anim & disable buttons
        // 2. On anim end: characterManager.next(), loadCurrentCharacterDisplay(false), isCharacterAnimating=false, enable buttons.
        // --- Using your provided animation logic directly: ---
        if (backgroundThunderImageView == null || backgroundSpotlightCircle == null ||
            characterImageView == null || characterNameLabel == null || menuButtonBox == null || centerContentVBox == null || tradeMarc == null) {
            System.err.println("handleCharacterChange: One or more critical FXML elements for animation are null!");
            isCharacterAnimating = false; setMenuButtonsDisabled(false); // Re-enable if bailing
            return;
        }
        // (Your Durations...)
        Duration spotlightFadeOutDuration = Duration.millis(250);
        Duration mainElementsFadeDuration = Duration.millis(400);
        // ... rest of your detailed animation sequence code as you provided it ...
        // The end of that sequence should correctly set isCharacterAnimating = false and setMenuButtonsDisabled(false).
        // That complex animation sequence is copied from your previous MainMenuController.java.
        FadeTransition spotlightFadeOut = createFadeTransition(
                backgroundSpotlightCircle, spotlightFadeOutDuration, backgroundSpotlightCircle.getOpacity(), 0.0);
        ParallelTransition allOtherElementsFadeOut = new ParallelTransition();
        allOtherElementsFadeOut.getChildren().add(createFadeTransition(backgroundThunderImageView, mainElementsFadeDuration, backgroundThunderImageView.getOpacity(), 0.0));
        allOtherElementsFadeOut.getChildren().add(createFadeTransition(menuButtonBox, mainElementsFadeDuration, menuButtonBox.getOpacity(), 0.0));
        allOtherElementsFadeOut.getChildren().add(createFadeTransition(centerContentVBox, mainElementsFadeDuration, centerContentVBox.getOpacity(), 0.0)); 
        allOtherElementsFadeOut.getChildren().add(createFadeTransition(tradeMarc, mainElementsFadeDuration, tradeMarc.getOpacity(), 0.0));
        SequentialTransition fullFadeOutToBlack = new SequentialTransition(spotlightFadeOut, createPauseTransition(Duration.millis(100)),allOtherElementsFadeOut,createPauseTransition(Duration.millis(100)));

        fullFadeOutToBlack.setOnFinished(fadeOutEvent -> {
            characterManager.nextCharacter();
            if(characterImageView != null) characterImageView.setOpacity(0.0);
            if(characterNameLabel != null) { characterNameLabel.setOpacity(0.0); characterNameLabel.setText(""); }
            if(backgroundSpotlightCircle != null) backgroundSpotlightCircle.setOpacity(0.0);
            loadCurrentCharacterDisplay(false); 
            ParallelTransition allElementsFadeIn = new ParallelTransition();
            allElementsFadeIn.getChildren().add(createFadeTransition(backgroundThunderImageView, mainElementsFadeDuration, 0.0, VISIBLE_BACKGROUND_OPACITY));
            allElementsFadeIn.getChildren().add(createBlinkTimeline(backgroundSpotlightCircle, Duration.millis(100), 1.0));
            allElementsFadeIn.getChildren().add(createBlinkTimeline(menuButtonBox, Duration.millis(120), 1.0));
            allElementsFadeIn.getChildren().add(createBlinkTimeline(centerContentVBox, Duration.millis(120), 1.0));
            allElementsFadeIn.getChildren().add(createBlinkTimeline(tradeMarc, Duration.millis(120), 1.0));
            allElementsFadeIn.getChildren().add(createBlinkTimeline(characterImageView, Duration.millis(100), 1.0));
            allElementsFadeIn.getChildren().add(createFadeTransition(characterNameLabel, Duration.millis(500), 0.0, 1.0));
            SequentialTransition showEverythingSequence = new SequentialTransition(createPauseTransition(Duration.millis(150)), allElementsFadeIn);
            showEverythingSequence.play();
            showEverythingSequence.setOnFinished(fadeInEvent -> {
                isCharacterAnimating = false;
                setMenuButtonsDisabled(false);
            });
        });
        fullFadeOutToBlack.play();
    }


    @FXML
    public void handleNewGame(ActionEvent event) {
        System.out.println("MainMenuController: handleNewGame called!");

        ensureMainMenuContentIsSetup(); // Ensure manager is definitely ready
        if (!mainMenuContentIsSetup || this.characterManager == null) {
            System.err.println("MainMenuController CRITICAL: Main menu content not setup OR CharacterManager is NULL in handleNewGame. Aborting. mainMenuContentIsSetup=" + mainMenuContentIsSetup);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Menu Error");
            alert.setHeaderText("Menu Not Fully Initialized");
            alert.setContentText("Please wait a moment for the menu to load and try again.");
            alert.showAndWait();
            return;
        }
        System.out.println("MainMenuController: handleNewGame - Proceeding, CharacterManager IS available.");

        GameManager gm = GameManager.getInstance();
        String selectedCharacterName = this.characterManager.getCurrentName(); // From local CharacterManager
        
        if (selectedCharacterName == null || selectedCharacterName.trim().isEmpty()) {
            System.err.println("MainMenuController CRITICAL: Could not get a valid character name from CharacterManager instance.");
            selectedCharacterName = "FallbackPlayer"; // Should not happen if CharacterManager works
        }
        gm.setCurrentPlayerCharacterName(selectedCharacterName);

        String portraitBaseName = selectedCharacterName.toLowerCase();
        // Make sure this path aligns with your actual portrait image locations and names
        String selectedPlayerPortraitPath = "/com/leave/engine/images/characters/portraits/" + portraitBaseName + ".png";
        gm.setCurrentPlayerPortraitPath(selectedPlayerPortraitPath);

        System.out.println("MainMenuController: Player selected: " + selectedCharacterName +
                           ", Dialogue Portrait Path set to GameManager: " + selectedPlayerPortraitPath);

        if (thunderAnimator != null) thunderAnimator.stop();
        if (logoAnimator != null) logoAnimator.stop();

        try {
            App.setRoot("gameplay", (controller) -> {
                if (controller instanceof GamePlayController) {
                    GamePlayController gpc = (GamePlayController) controller;
                    System.out.println("MainMenuController: Transitioning to gameplay. Calling gpc.displayCurrentScene().");
                    gpc.displayCurrentScene();
                }
            });
        } catch (IOException e) {
            System.err.println("MainMenuController Error: loading gameplay.fxml for new game: " + e.getMessage());
            e.printStackTrace();
            // TODO: Show user-friendly error dialog
        }
    }

    @FXML
    public void handleLoadGame(ActionEvent event) { // This is "Quit Game"
        System.out.println("MainMenuController: Quit Game button clicked.");
        if (thunderAnimator != null) thunderAnimator.stop();
        if (logoAnimator != null) logoAnimator.stop();
        //if (audioManager != null) audioManager.shutdown(); // Assuming you have AudioManager instance here or get it
        Platform.exit();
        System.exit(0); // Force exit if Platform.exit() hangs for any reason
    }
}