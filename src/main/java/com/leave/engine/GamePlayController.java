package com.leave.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.leave.engine.utils.AnimationUtils.animateText; // Ensure this utility exists and works
import com.leave.engine.utils.AudioManager;
import com.leave.engine.utils.SpriteSheetAnimator;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class GamePlayController implements Initializable {

    // --- FXML Injections ---
    @FXML private StackPane gameRootPane;
    @FXML private ImageView sceneBackgroundImageView;
    @FXML private ImageView characterDisplayImageView;      
    @FXML private HBox dialogueHudHBox;                 
    @FXML private StackPane dialogueAndChoicesStack;    
    @FXML private VBox dialogueTextContainer;           // For dialogue text + indicator (naks of dialogueAndChoicesStack)
    @FXML private Label sceneTextLabel;                 
    @FXML private ImageView continueIndicatorImageView;
    @FXML private VBox speakerPortraitContainer;      
    @FXML private ImageView speakerPortraitImageView;  
    @FXML private Label speakerNameLabel;              
    @FXML private VBox choicesVBox;                     // Holds choice buttons (child of dialogueAndChoicesStack)
    @FXML private Label endingTitleLabel; 

    private GameManager gameManager;
    private AudioManager audioManager;
    private SpriteSheetAnimator currentBackgroundAnimator;
    private SpriteSheetAnimator currentCharacterAnimator; 

    private List<DialogueEntry> currentSceneDialogueLines;
    private int currentDialogueLineIndex;
    private boolean dialogueAnimationPlaying = false;
    private boolean waitingForClickToAdvanceDialogue = false;

    private static final String CONTINUE_INDICATOR_PATH = "/com/leave/engine/images/ui/continue_arrow.png ";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.gameManager = GameManager.getInstance();
        this.audioManager = AudioManager.getInstance();
        System.out.println("GPC: Initializing Controller...");

   
        // Initial UI state setup
        if (dialogueHudHBox != null) dialogueHudHBox.setVisible(true); // Main HUD container is usually visible
        showDialogueArea(); // Default to showing dialogue text area, choices hidden
        clearAndHidePortrait();     // Ensure portrait area is initially clean & hidden

        // Load continue indicator image
        try (InputStream stream = getClass().getResourceAsStream(CONTINUE_INDICATOR_PATH)) {
            if (stream != null && continueIndicatorImageView != null) {
                continueIndicatorImageView.setImage(new Image(stream));
                continueIndicatorImageView.setVisible(false); // Start hidden
            } else {
                if (continueIndicatorImageView == null) System.err.println("GPC: continueIndicatorImageView is null.");
                else System.err.println("GPC: Continue indicator image resource not found: " + CONTINUE_INDICATOR_PATH);
            }
        } catch (Exception e) {
            System.err.println("GPC: Error loading continue indicator image: " + e.getMessage());
        }

        // Initialize other UI elements to a default hidden/empty state
        if (characterDisplayImageView != null) {
            characterDisplayImageView.setVisible(false);
            characterDisplayImageView.setManaged(false);
        }
        if (sceneTextLabel != null) sceneTextLabel.setText("");

        // Setup global click listener for advancing dialogue
        if (gameRootPane != null) {
            gameRootPane.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleScreenClick);
        } else {
            System.err.println("GPC Error: gameRootPane is null. Cannot setup screen click listener.");
        }
        System.out.println("GPC: Initialization complete.");
        // displayCurrentScene() is typically called by App.java via a callback
        // after the MainMenuController transitions to this scene.
    }

    // --- UI State Management Helpers ---

   private void showDialogueArea() {
    if (dialogueAndChoicesStack == null) {
        System.err.println("GPC Error: dialogueAndChoicesStack is null in showDialogueArea.");
        return;
    }
    if (dialogueTextContainer != null) {
        dialogueTextContainer.setVisible(true);
        dialogueTextContainer.setManaged(true); 
    }
    if (choicesVBox != null) {
        choicesVBox.setVisible(false);
        choicesVBox.setManaged(false); 
        choicesVBox.setDisable(true);
    }
    
    if (dialogueTextContainer != null) dialogueTextContainer.toFront();
    System.out.println("GPC: UI Switched to: Dialogue Area visible in StackPane.");
}


    

 private void showChoicesArea() {
    if (dialogueAndChoicesStack == null) {
        System.err.println("GPC Error: dialogueAndChoicesStack is null in showChoicesArea.");
        return;
    }
    if (dialogueTextContainer != null) {
        dialogueTextContainer.setVisible(false);
        dialogueTextContainer.setManaged(false);
    }
    if (choicesVBox != null) {
        choicesVBox.setVisible(true);
        choicesVBox.setManaged(true);
        choicesVBox.setDisable(false); // Enable for interaction
    }
    // Bring choicesVBox to the front of the StackPane
    if (choicesVBox != null) choicesVBox.toFront();
    System.out.println("GPC: UI Switched to: Choices Area visible in StackPane.");
}
    private void clearAndHidePortrait() {
        if (speakerPortraitImageView != null) speakerPortraitImageView.setImage(null);
        if (speakerNameLabel != null) {
            speakerNameLabel.setText("");
            speakerNameLabel.setVisible(false);
        }
        if (speakerPortraitContainer != null) {
            speakerPortraitContainer.setVisible(false);
            speakerPortraitContainer.setManaged(false);
        }
    }

        // --- Core Scene Display Logic ---

    public void displayCurrentScene() {
    
    SceneData currentSceneData = gameManager.getCurrentSceneData();
    String sceneIdForLog = (currentSceneData != null) ? currentSceneData.getId() : (gameManager != null ? gameManager.getCurrentSceneId() : "NULL_SCENE_ID");
    System.out.println("GPC: displayCurrentScene() for scene ID: " + sceneIdForLog);

    
    if (endingTitleLabel != null) {
        endingTitleLabel.setText(""); // Clear previous title
        endingTitleLabel.setVisible(false);
        endingTitleLabel.setManaged(false);
    }
   
    // If choicesVBox is sometimes left visible from a previous state (e.g. error),
    // you might consider also explicitly hiding it here or ensuring showDialogueArea() is robust.
    // if (choicesVBox != null) { choicesVBox.setVisible(false); choicesVBox.setManaged(false); }


    // 3. Handle case where scene data is missing
    if (currentSceneData == null) {
        System.err.println("GPC: Critical - currentSceneData is null. Cannot display scene.");
        if (sceneTextLabel != null) sceneTextLabel.setText("ERROR: SCENE DATA MISSING OR CORRUPT.");
        if (dialogueHudHBox != null) dialogueHudHBox.setVisible(true); // Ensure HUD is visible for error
        showDialogueArea(); // Show some default UI state
        // Potentially show the fxmlStaticExitButton here too for a hard error state?
        // if (fxmlStaticExitButton != null) {
        //     fxmlStaticExitButton.setVisible(true);
        //     fxmlStaticExitButton.setManaged(true);
        // }
        return;
    }

    
    //    This happens AFTER the reset, so it only shows if the current scene requires it.
    if (endingTitleLabel != null) {
        if (currentSceneData.getEndingTitle() != null && !currentSceneData.getEndingTitle().trim().isEmpty()) {
            endingTitleLabel.setText(currentSceneData.getEndingTitle());
            endingTitleLabel.setVisible(true);
            endingTitleLabel.setManaged(true);
            System.out.println("GPC: Displaying ending title: " + currentSceneData.getEndingTitle());
        }
        // No 'else' needed here to hide it, as it was reset at the top of the method.
    }

    // 5. Reset general UI components for the new scene (dialogue area, portrait, animators etc.)
    if (dialogueHudHBox != null) dialogueHudHBox.setVisible(true);
    showDialogueArea(); // Ensure dialogue text area is active, choices hidden

    clearAndHidePortrait(); // Clear previous portrait/name

    if (currentBackgroundAnimator != null) { currentBackgroundAnimator.stop(); currentBackgroundAnimator = null; }
    if (currentCharacterAnimator != null) { currentCharacterAnimator.stop(); currentCharacterAnimator = null; }

    dialogueAnimationPlaying = false;
    waitingForClickToAdvanceDialogue = false;
    if (continueIndicatorImageView != null) continueIndicatorImageView.setVisible(false);
    if (sceneTextLabel != null) sceneTextLabel.setText(""); // Clear previous scene text

    // 6. Load and set scene content (background, character, music)
    setBackground(currentSceneData.getBackgroundSprite(), currentSceneData.getBackgroundImage());
    setSpeakerAndCharacterVisibility(currentSceneData.getCharacterSprite() != null, currentSceneData.getCharacterSprite()); // For on-screen sprite

    String bgmPath = currentSceneData.getBackgroundMusic();
    if (bgmPath != null && !bgmPath.trim().isEmpty()) {
        audioManager.playBackgroundMusic(bgmPath, true, 0.6);
    } else {
        audioManager.stopBackgroundMusic();
    }

    // 7. Initialize dialogue
    this.currentSceneDialogueLines = currentSceneData.getDialogue();
    this.currentDialogueLineIndex = 0;

    if (this.currentSceneDialogueLines != null && !this.currentSceneDialogueLines.isEmpty()) {
        System.out.println("GPC: Scene " + sceneIdForLog + " has dialogue. Starting...");
        showNextDialogueLine();
    } else {
        System.out.println("GPC: Scene " + sceneIdForLog + " has no dialogue. Processing end of scene logic.");
        processEndOfSceneLogic(currentSceneData);
    }
}

    

    private void setBackground(SpriteInfo bgSpriteInfo, String staticBgImagePath) {
        if (sceneBackgroundImageView == null) return;
        sceneBackgroundImageView.setImage(null);
        sceneBackgroundImageView.setViewport(null);
        sceneBackgroundImageView.setVisible(false);

        if (currentBackgroundAnimator != null) {
            currentBackgroundAnimator.stop();
            currentBackgroundAnimator = null;
        }

        boolean backgroundSet = false;
        // Try using SpriteInfo (could be animated or a single-frame spritesheet)
        if (bgSpriteInfo != null && bgSpriteInfo.getPath() != null && !bgSpriteInfo.getPath().trim().isEmpty()) {
            if (bgSpriteInfo.getFrameWidth() > 0 && bgSpriteInfo.getTotalFrames() > 0 &&
                bgSpriteInfo.getFps() > 0 && bgSpriteInfo.getNumCols() > 0) { // Valid animation data
                try {
                    currentBackgroundAnimator = new SpriteSheetAnimator(
                        sceneBackgroundImageView, bgSpriteInfo.getPath(),
                        bgSpriteInfo.getFrameWidth(), bgSpriteInfo.getFrameHeight(),
                        bgSpriteInfo.getNumCols(), bgSpriteInfo.getTotalFrames(),
                        bgSpriteInfo.getFps(), bgSpriteInfo.isLoop()
                    );
                    sceneBackgroundImageView.setVisible(true);
                    currentBackgroundAnimator.play();
                    System.out.println("GPC: Set background (animated/spritesheet): " + bgSpriteInfo.getPath());
                    backgroundSet = true;
                } catch (Exception e) {
                    System.err.println("GPC Error setting animated/spritesheet background from SpriteInfo: " + bgSpriteInfo.getPath() + " - " + e.getMessage());
                    // Attempt to load as static if animator failed with its own path
                    loadStaticImageToView(bgSpriteInfo.getPath(), sceneBackgroundImageView, "GPC: Fallback to static from SpriteInfo.path: ");
                    backgroundSet = sceneBackgroundImageView.getImage() != null;
                }
            } else { // SpriteInfo present but not enough data for animation, treat path as static
                loadStaticImageToView(bgSpriteInfo.getPath(), sceneBackgroundImageView, "GPC: Set static background (from SpriteInfo.path, non-animated): ");
                backgroundSet = sceneBackgroundImageView.getImage() != null;
            }
        }

        // Fallback to dedicated staticBackgroundImagePath if bgSpriteInfo didn't provide a usable background
        if (!backgroundSet && staticBgImagePath != null && !staticBgImagePath.trim().isEmpty()) {
            loadStaticImageToView(staticBgImagePath, sceneBackgroundImageView, "GPC: Set static background (from backgroundImage field): ");
            backgroundSet = sceneBackgroundImageView.getImage() != null;
        }

        if (!backgroundSet) {
            System.out.println("GPC: No background specified or failed to load for the current scene.");
        }
    }

    private void loadStaticImageToView(String imagePath, ImageView imageView, String logPrefix) {
        if (imagePath == null || imagePath.trim().isEmpty() || imageView == null) {
             System.err.println(logPrefix + "Skipping load: imagePath or imageView is null.");
             return;
        }
        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream != null) {
                Image staticImg = new Image(stream);
                if (staticImg.isError()) {
                    System.err.println(logPrefix + "Error in Image object after loading: " + imagePath + ". Exception: " + staticImg.getException().getMessage());
                    imageView.setImage(null);
                    imageView.setVisible(false);
                } else {
                    imageView.setImage(staticImg);
                    imageView.setVisible(true);
                    System.out.println(logPrefix + "Loaded successfully: " + imagePath);
                }
            } else {
                System.err.println(logPrefix + "Static image resource not found in classpath: " + imagePath);
                imageView.setImage(null);
                imageView.setVisible(false);
            }
        } catch (Exception e) {
            System.err.println(logPrefix + "Exception during static image load for " + imagePath + ": " + e.getMessage());
            imageView.setImage(null);
            imageView.setVisible(false);
        }
    }

    // Manages the on-screen character sprite (characterDisplayImageView)
    private void setSpeakerAndCharacterVisibility(boolean showCharacter, CharacterSpriteInfo charInfo) {
        if (characterDisplayImageView == null) return;

        // Reset previous state
        if (currentCharacterAnimator != null) {
            currentCharacterAnimator.stop();
            currentCharacterAnimator = null;
        }
        characterDisplayImageView.setImage(null);
        characterDisplayImageView.setViewport(null);
        characterDisplayImageView.setTranslateX(0); // Reset position
        characterDisplayImageView.setTranslateY(0);

        if (showCharacter && charInfo != null && charInfo.getPath() != null &&
            charInfo.getFrameWidth() > 0 && charInfo.getTotalFrames() > 0 && charInfo.getFps() > 0) {
            try {
                currentCharacterAnimator = new SpriteSheetAnimator(
                    characterDisplayImageView, charInfo.getPath(),
                    charInfo.getFrameWidth(), charInfo.getFrameHeight(),
                    charInfo.getNumCols(), charInfo.getTotalFrames(),
                    charInfo.getFps(), charInfo.isLoop()
                );

                // Apply position if specified
                characterDisplayImageView.setTranslateX(charInfo.getPositionX());
                characterDisplayImageView.setTranslateY(charInfo.getPositionY());

                characterDisplayImageView.setVisible(true);
                characterDisplayImageView.setManaged(true);
                currentCharacterAnimator.play();
                System.out.println("GPC: Set on-screen character (animator): " + charInfo.getPath() +
                                   " at X=" + charInfo.getPositionX() + ", Y=" + charInfo.getPositionY());
            } catch (Exception e) {
                System.err.println("GPC Error setting on-screen character sprite (animator): " + charInfo.getPath() + " - " + e.getMessage());
                characterDisplayImageView.setVisible(false);
                characterDisplayImageView.setManaged(false);
            }
        } else {
            characterDisplayImageView.setVisible(false);
            characterDisplayImageView.setManaged(false);
        }
    }

    // --- Dialogue Processing ---

    // In GamePlayController.java

private void showNextDialogueLine() {
    // Log the state of GameManager's currentSceneId AT THE VERY START OF THIS METHOD
    String gmCurrentSceneIdBeforeFetch = null;
    if (gameManager != null) { // Null check for gameManager
        gmCurrentSceneIdBeforeFetch = gameManager.getCurrentSceneId();
    } else {
        System.err.println("GPC ShowNextDialogueLine - START - CRITICAL: GameManager instance is null!");
        // Potentially handle this catastrophic failure, e.g., by showing an error and disabling further interaction.
        // For now, we'll let it proceed to the sceneContext check which will also fail.
    }
    System.out.println("GPC ShowNextDialogueLine - START - GameManager's currentSceneId before fetch: '" + gmCurrentSceneIdBeforeFetch + "'");

    final SceneData sceneContext = (gameManager != null) ? gameManager.getCurrentSceneData() : null;

    // Log what was fetched for sceneContext IMMEDIATELY
    if (sceneContext == null) {
        System.err.println("GPC ShowNextDialogueLine: CRITICAL - sceneContext IS NULL immediately after fetch from GameManager (GM's current ID was '" + gmCurrentSceneIdBeforeFetch + "').");
        System.err.println("GPC ShowNextDialogueLine: This means either the scene ID is invalid/not in JSON map, or a fundamental issue exists.");
        // Attempt to process end of (what we thought was) the scene, possibly showing an error.
        processEndOfSceneLogic(null); // Pass null to indicate the scene data couldn't be retrieved.
        return; // Abort further dialogue processing for this line.
    }

    // If sceneContext is NOT null, we can get its ID for logging.
    final String currentProcessingSceneId = sceneContext.getId();
    System.out.println("GPC ShowNextDialogueLine: Successfully fetched sceneContext. Processing dialogue for scene ID: '" + currentProcessingSceneId + "'");


    // Check if currentSceneDialogueLines (class member) matches the current context, or if dialogue is finished
    if (this.currentSceneDialogueLines == null || this.currentDialogueLineIndex >= this.currentSceneDialogueLines.size()) {
        // This typically means all dialogue for *this specific scene block* has been displayed.
        System.out.println("GPC ShowNextDialogueLine: End of dialogue lines OR currentSceneDialogueLines list is null for scene '" + currentProcessingSceneId + "'. currentDialogueLineIndex=" + currentDialogueLineIndex + ". Calling processEndOfSceneLogic.");
        this.waitingForClickToAdvanceDialogue = false;
        if (this.continueIndicatorImageView != null) this.continueIndicatorImageView.setVisible(false);
        processEndOfSceneLogic(sceneContext); // Pass the VALID (and current) sceneContext
        return;
    }

    showDialogueArea(); // Ensure dialogue UI components (dialogueTextContainer) are visible and on top.

    System.out.println("GPC ShowNextDialogueLine: Displaying line " + (this.currentDialogueLineIndex + 1) + "/" +
                       this.currentSceneDialogueLines.size() + " for scene '" + currentProcessingSceneId + "'");

    this.dialogueAnimationPlaying = true;
    this.waitingForClickToAdvanceDialogue = false;
    if (this.continueIndicatorImageView != null) this.continueIndicatorImageView.setVisible(false);

    DialogueEntry dialogueEntry = this.currentSceneDialogueLines.get(this.currentDialogueLineIndex);
    if (dialogueEntry == null) {
        System.err.println("GPC ShowNextDialogueLine: CRITICAL - DialogueEntry at index " + this.currentDialogueLineIndex + " is NULL for scene '" + currentProcessingSceneId + "'.");
        this.currentDialogueLineIndex++; // Try to skip this null entry
        showNextDialogueLine(); // Attempt to show the next line if any, or trigger end of scene
        return;
    }

    String rawSpeaker = dialogueEntry.getSpeaker();
    String processedSpeakerDisplayName = (gameManager != null) ? gameManager.processText(rawSpeaker) : rawSpeaker; // Handle null GM
    String processedLine = (gameManager != null) ? gameManager.processText(dialogueEntry.getLine()) : dialogueEntry.getLine();


    // Determine if speaker nameplate should be shown
    boolean showNamePlate = rawSpeaker != null && !rawSpeaker.trim().isEmpty() &&
                           !rawSpeaker.equalsIgnoreCase("Narrator");

    // Get portrait path
    String portraitPath = null;
    if (gameManager != null && gameManager.getPlayerNamePlaceholderFromStory() != null &&
        gameManager.getPlayerNamePlaceholderFromStory().equals(rawSpeaker)) {
        portraitPath = gameManager.getCurrentPlayerPortraitPath(); // Assumes method is implemented in GameManager
        if (portraitPath == null || portraitPath.trim().isEmpty()) {
            System.err.println("GPC: Player is speaker ('" + processedSpeakerDisplayName + "'), but dynamic portrait path from GameManager is invalid/null. Using placeholder.");
            portraitPath = "/com/leave/engine/images/characters/portraits/player_placeholder.png"; // Default placeholder
        } else {
            System.out.println("GPC: Player ('" + processedSpeakerDisplayName + "') speaking. Dynamic portrait: " + portraitPath);
        }
    } else {
        portraitPath = dialogueEntry.getPortraitPath(); // From JSON for NPCs
         if (portraitPath != null && !portraitPath.trim().isEmpty()) {
             System.out.println("GPC: NPC ('" + processedSpeakerDisplayName + "') speaking. Portrait from JSON: " + portraitPath);
         } else {
             // System.out.println("GPC: NPC ('"+processedSpeakerDisplayName+"') has no portraitPath in JSON.");
         }
    }

    // Display Portrait
    if (this.speakerPortraitContainer != null && this.speakerPortraitImageView != null) {
        if (portraitPath != null && !portraitPath.trim().isEmpty()) {
            loadStaticImageToView(portraitPath, this.speakerPortraitImageView, "GPC Dialogue Portrait: ");
            if (this.speakerPortraitImageView.getImage() != null && !this.speakerPortraitImageView.getImage().isError()) {
                this.speakerPortraitContainer.setVisible(true);
                this.speakerPortraitContainer.setManaged(true);
            } else {
                clearAndHidePortrait(); // Loading image failed
            }
        } else {
            clearAndHidePortrait(); // No portraitPath provided or resolved
        }
    }

    // Display Speaker Nameplate
    if (this.speakerNameLabel != null) {
        if (showNamePlate && this.speakerPortraitContainer != null && this.speakerPortraitContainer.isVisible()) {
            this.speakerNameLabel.setText(processedSpeakerDisplayName);
            this.speakerNameLabel.setVisible(true);
        } else {
            this.speakerNameLabel.setText("");
            this.speakerNameLabel.setVisible(false);
        }
    }

    // Animate Text
    if (this.sceneTextLabel != null) {
        // Re-confirm sceneContext before lambda, just in case (extreme paranoia)
        if (sceneContext == null) {
             System.err.println("GPC ShowNextDialogueLine: PARANOIA CHECK - sceneContext BECAME NULL just BEFORE creating animateText lambda for what was scene ID: " + currentProcessingSceneId + " ! This implies severe state corruption.");
             processEndOfSceneLogic(null); // Indicate failure to proceed
             return;
        }
        // final String capturedSceneIdForAnimLambda = sceneContext.getId(); // Already have currentProcessingSceneId

        System.out.println("GPC ShowNextDialogueLine: Creating animateText for line: \"" + processedLine.substring(0, Math.min(processedLine.length(), 30)) + "...\" for scene '" + currentProcessingSceneId + "'");
        animateText(this.sceneTextLabel, processedLine, 30, () -> {
            this.dialogueAnimationPlaying = false;
            // It's crucial that 'sceneContext' (the final variable) is used here to ensure we operate on the correct scene's data,
            // especially if scene transitions could happen rapidly or if callbacks get queued.
            String idInCallback = (sceneContext != null && sceneContext.getId() != null) ? sceneContext.getId() : "SCENE_CONTEXT_NOW_NULL_IN_CALLBACK";
            System.out.println("GPC animateText CB: Finished animating line " + this.currentDialogueLineIndex + " (0-indexed) for scene: " + idInCallback);

            this.currentDialogueLineIndex++;

            if (this.currentDialogueLineIndex < this.currentSceneDialogueLines.size()) {
                System.out.println("GPC animateText CB: More lines remain for scene " + idInCallback + ". Setting wait for click. Next index: " + this.currentDialogueLineIndex);
                this.waitingForClickToAdvanceDialogue = true;
                if (this.continueIndicatorImageView != null) this.continueIndicatorImageView.setVisible(true);
            } else {
                // This was the LAST line of dialogue for this specific scene (sceneContext)
                System.out.println("GPC animateText CB: Last dialogue line for scene " + idInCallback + " finished (index " + this.currentDialogueLineIndex + " equals size " + this.currentSceneDialogueLines.size() + "). Calling processEndOfSceneLogic.");
                this.waitingForClickToAdvanceDialogue = false;
                if (this.continueIndicatorImageView != null) this.continueIndicatorImageView.setVisible(false);
                if (this.sceneTextLabel != null) this.sceneTextLabel.setText(processedLine); // Ensure full text of last line is set

                if (sceneContext == null) { // Final paranoia check before passing
                     System.err.println("GPC animateText CB: CRITICAL - captured 'final sceneContext' IS NULL before passing to processEndOfSceneLogic for originally processed scene " + idInCallback);
                }
                processEndOfSceneLogic(sceneContext); // Use the originally captured sceneContext for this dialogue sequence
            }
        });
    } else {
        System.err.println("GPC ShowNextDialogueLine: sceneTextLabel is null. Cannot animate text for scene " + currentProcessingSceneId + ". Advancing logic.");
        this.dialogueAnimationPlaying = false;
        this.currentDialogueLineIndex++;
        if (this.currentDialogueLineIndex < this.currentSceneDialogueLines.size()) {
            this.waitingForClickToAdvanceDialogue = true;
        } else {
            processEndOfSceneLogic(sceneContext); // Process end with the current sceneContext
        }
    }
}


private void handleScreenClick(MouseEvent event) {
    // ...
    if (waitingForClickToAdvanceDialogue) {
        // ...
        waitingForClickToAdvanceDialogue = false;
        // ...
        
        // Check if currentDialogueLineIndex is now BEYOND the last line,
        // meaning the click was to advance PAST the last line of an ending scene.
        if (currentSceneDialogueLines != null && currentDialogueLineIndex >= currentSceneDialogueLines.size()) {
            SceneData currentScene = gameManager.getCurrentSceneData();
            if (currentScene.getId() != null && currentScene.getId().startsWith("ending_")) {
                System.out.println("GPC handleScreenClick: Click after last line of ending scene. Processing buttons.");
                processEndOfSceneLogic(currentScene); // Now process it, which will show buttons
                event.consume();
                return; // IMPORTANT: prevent calling showNextDialogueLine which would be out of bounds
            }
        }
        // Otherwise, if there are still lines, call showNextDialogueLine()
        if (currentSceneDialogueLines != null && currentDialogueLineIndex < currentSceneDialogueLines.size()){
             showNextDialogueLine();
        } else {
             // This case might indicate end of dialogue for a non-ending scene if not handled by animateText CB properly
             processEndOfSceneLogic(gameManager.getCurrentSceneData());
        }
        event.consume();
    }
}
    // --- Scene Completion and Transition Logic ---

     private void processEndOfSceneLogic(SceneData scene) {
        if (scene == null) {
            System.err.println("GPC: processEndOfSceneLogic - scene is null! Fallback or error display needed.");
            if (sceneTextLabel != null) sceneTextLabel.setText("CRITICAL ERROR: Scene context lost!");
            showDialogueArea();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Critical Error");
            errorAlert.setHeaderText("A critical error occurred.");
            errorAlert.setContentText("The game cannot continue and will now close.");
            if (gameRootPane != null && gameRootPane.getScene() != null && gameRootPane.getScene().getWindow() != null) {
                errorAlert.initOwner(gameRootPane.getScene().getWindow());
            }
            errorAlert.showAndWait();
            Platform.exit();
            System.exit(1);
            return;
        }
        String sceneId = scene.getId();
        System.out.println("GPC: processEndOfSceneLogic for scene: " + sceneId);

        clearAndHidePortrait();

        if (scene.getAction() != null && !scene.getAction().trim().isEmpty()) {
            System.out.println("GPC: Processing scene action '" + scene.getAction() + "' for scene " + sceneId);
            gameManager.processAction(scene.getAction());
        }

        if (scene.getChoices() != null && !scene.getChoices().isEmpty()) {
            System.out.println("GPC: Scene " + sceneId + " has CHOICES.");
            populateAndShowChoices(scene);
        } else if (scene.getAutoTransitionTo() != null && !scene.getAutoTransitionTo().trim().isEmpty()) {
            System.out.println("GPC: Scene " + sceneId + " has AUTO-TRANSITION to: " + scene.getAutoTransitionTo());
            showDialogueArea();
            gameManager.advanceToScene(scene.getAutoTransitionTo());
            displayCurrentScene();
        } else if (scene.getOutcome() != null && !scene.getOutcome().trim().isEmpty()) {
            System.out.println("GPC: Scene " + sceneId + " leads to OUTCOME: " + scene.getOutcome());
            gameManager.setGameOver(scene.getOutcome());
            displayOutcome();
        } else {
            // No choices, no auto-transition, no direct outcome from THIS scene object.
            if (sceneId != null && (sceneId.startsWith("ending_") || sceneId.startsWith("ending"))) {
                // --- THIS IS THE ENDING SCENE LOGIC (message and exit) ---
                System.out.println("GPC: Scene " + sceneId + " is an ending scene. Displaying final text, then showing completion message and exiting.");
                showDialogueArea();

                if (sceneTextLabel != null && (sceneTextLabel.getText() == null || sceneTextLabel.getText().trim().isEmpty())) {
                    if (currentSceneDialogueLines != null && !currentSceneDialogueLines.isEmpty()) {
                        if (currentSceneDialogueLines.size() > 0) {
                            sceneTextLabel.setText(gameManager.processText(currentSceneDialogueLines.get(currentSceneDialogueLines.size() - 1).getLine()));
                            System.out.println("GPC: (Fallback: Set last ending text to: " + sceneTextLabel.getText() + ")");
                        } else {
                             sceneTextLabel.setText("The end.");
                        }
                    } else {
                        sceneTextLabel.setText("The end.");
                    }
                }

                PauseTransition delayBeforeMessage = new PauseTransition(Duration.millis(2000));
                delayBeforeMessage.setOnFinished(finishedEvent -> {
                    System.out.println("GPC: Delay finished for ending scene " + sceneId + ". Showing completion message and exiting.");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Game Finished!");
                    alert.setHeaderText(null);
                    alert.setContentText("You finished the game!");
                    if (gameRootPane != null && gameRootPane.getScene() != null && gameRootPane.getScene().getWindow() != null) {
                        alert.initOwner(gameRootPane.getScene().getWindow());
                    }
                    alert.showAndWait();
                    System.out.println("GPC: Exiting game after completion message.");
                    if (audioManager != null) audioManager.shutdown();
                    Platform.exit();
                    System.exit(0);
                });
                delayBeforeMessage.play();
            } else {
                // --- THIS IS THE "NARRATIVE DEAD END" SCENE LOGIC ---
                System.out.println("GPC: Scene " + sceneId + " ends here narratively (no further navigation defined in JSON, and not an 'ending_' scene).");
                showDialogueArea(); // Make sure dialogue area is visible

                String deadEndMessage = "The story pauses here. You've reached a point with no further defined path in the current game data.";
                if (sceneTextLabel != null) {
                    sceneTextLabel.setText(deadEndMessage);
                    System.out.println("GPC: Displayed dead-end message: \"" + deadEndMessage + "\"");
                } else {
                    System.err.println("GPC: sceneTextLabel is null, cannot display dead-end message on screen.");
                }

                // Pause for a moment to let the player read the message on screen
                PauseTransition delayBeforeAlert = new PauseTransition(Duration.millis(3000)); // 3 seconds
                delayBeforeAlert.setOnFinished(event -> {
                    Alert deadEndAlert = new Alert(Alert.AlertType.INFORMATION);
                    deadEndAlert.setTitle("Narrative End");
                    deadEndAlert.setHeaderText("You've reached a narrative dead end.");
                    deadEndAlert.setContentText("The game does not continue from this point in the current story data. The application will now offer to close.");

                    // Optional: Add specific buttons if you want "Return to Menu" vs "Exit"
                    // For simplicity, an "OK" button leading to exit is fine.
                    // ButtonType exitButtonType = new ButtonType("Exit Game");
                    // deadEndAlert.getButtonTypes().setAll(exitButtonType);

                    if (gameRootPane != null && gameRootPane.getScene() != null && gameRootPane.getScene().getWindow() != null) {
                        deadEndAlert.initOwner(gameRootPane.getScene().getWindow());
                    }

                    deadEndAlert.showAndWait();
                    // .ifPresent(response -> {  // If you added custom buttons
                    //    if (response == exitButtonType) {
                            System.out.println("GPC: Exiting game after narrative dead end confirmation.");
                            if (audioManager != null) audioManager.shutdown();
                            Platform.exit();
                            System.exit(0);
                    //    }
                    // });
                });
                delayBeforeAlert.play();
            }
        }
    }
    
    private void populateAndShowChoices(SceneData sceneData) {
        if (choicesVBox == null || sceneData == null || sceneData.getChoices() == null) {
            System.err.println("GPC populateAndShowChoices: Critical - FXML choicesVBox, sceneData, or choices list is null.");
            showDialogueArea(); // Fallback to showing some UI
            if(sceneTextLabel != null) sceneTextLabel.setText("Error preparing choices.");
            return;
        }
        String sceneIdForLog = (sceneData.getId() != null) ? sceneData.getId() : "UNKNOWN";
        System.out.println("GPC: Populating choices for scene: " + sceneIdForLog);

        showChoicesArea(); 
        clearAndHidePortrait(); 
        if (sceneTextLabel != null) sceneTextLabel.setText(""); // Clear main dialogue text area

        choicesVBox.getChildren().clear();
        boolean atLeastOneChoiceAvailable = false;

        for (ChoiceData choice : sceneData.getChoices()) {
            boolean displayChoice = true;
            // Condition checking...
            // if (choice.getRequiredItem() != null && !choice.getRequiredItem().trim().isEmpty()) {
            //     if (!gameManager.hasItem(choice.getRequiredItem())) displayChoice = false;
            // }
            if (displayChoice && choice.getRequiredFlag() != null && !choice.getRequiredFlag().trim().isEmpty()) {
                if (!gameManager.checkFlag(choice.getRequiredFlag())) displayChoice = false;
            }

            if (displayChoice) {
                atLeastOneChoiceAvailable = true;
                Button choiceButton = new Button(gameManager.processText(choice.getText()));
                choiceButton.getStyleClass().add("choice-button"); // Ensure CSS applies
                choiceButton.setMaxWidth(Double.MAX_VALUE);
                choiceButton.setOnAction(event -> {
                    if (audioManager != null) audioManager.playSoundEffect("/com/leave/engine/audio/clicker.wav");
                    handleChoiceSelected(choice);
                });
                choicesVBox.getChildren().add(choiceButton);
            }
        }

        if (!atLeastOneChoiceAvailable) {
            System.out.println("GPC: No choices available for " + sceneIdForLog + " after conditions. Showing fallback message.");
            showDialogueArea(); // Switch back to dialogue display
            if (sceneTextLabel != null) sceneTextLabel.setText("There are no suitable options at this time.");
            // Consider next step: auto-transition to a "stuck" scene, or specific outcome?
        }
    }

    private void handleChoiceSelected(ChoiceData choice) {
        System.out.println("GPC: Choice selected: '" + choice.getText() + "'");
        if (choicesVBox != null) {
            choicesVBox.setDisable(true); // Prevent double clicks
            // Optionally hide choicesVBox immediately for visual feedback if transition is not instant
            // choicesVBox.setVisible(false);
            // choicesVBox.setManaged(false);
        }

        gameManager.makeChoice(choice); 

        // Defer UI update to ensure GameManager state change is processed
        Platform.runLater(() -> {
            if (gameManager.isGameOver()) {
                displayOutcome();
            } else {
                // If makeChoice simply set currentSceneId in GameManager for a normal scene
                displayCurrentScene();
            }
        });
    }

    private void displayOutcome() {
        System.out.println("GPC: Displaying outcome state.");

        showDialogueArea(); // Use dialogue area for the outcome message
        clearAndHidePortrait(); // No character portrait for generic outcome messages

        OutcomeData outcome = gameManager.getCurrentOutcomeData();
        if (outcome != null) {
            String outcomeMessage = gameManager.processText(outcome.getMessage());
            if (sceneTextLabel != null) {
                animateText(sceneTextLabel, outcomeMessage, 30, () -> {
                    // After outcome message animation finishes:
                    showChoicesArea(); // Switch to choices area for the buttons
                    if (choicesVBox != null) {
                        choicesVBox.getChildren().clear(); // Prepare for outcome buttons
                        if (outcome.getNextSceneId() != null && !outcome.getNextSceneId().trim().isEmpty()) {
                            Button continueButton = new Button("Continue...");
                            continueButton.getStyleClass().add("choice-button");
                            continueButton.setOnAction(e -> {
                                if (audioManager != null) audioManager.playSoundEffect("/com/leave/engine/audio/clicker.wav");
                                gameManager.resetGameOver(); // Reset if leading to a new playable part
                                gameManager.advanceToScene(outcome.getNextSceneId());
                                displayCurrentScene(); // Display the "ending_" scene
                            });
                            choicesVBox.getChildren().add(continueButton);
                        } else {
                            addTerminalOutcomeButtons(); // For outcomes that don't lead to another scene
                        }
                    } else {
                        System.err.println("GPC displayOutcome CB: choicesVBox is null. Cannot show outcome buttons.");
                    }
                });
            } else {
                 System.err.println("GPC displayOutcome: sceneTextLabel is null. Cannot display outcome message. Showing buttons immediately.");
                 showChoicesArea(); if (choicesVBox != null) { choicesVBox.getChildren().clear(); addTerminalOutcomeButtons(); }
            }
        } else {
            String errorMsg = "Game Over. (Critical: Outcome data missing for ID " + gameManager.getCurrentOutcomeId() + ")";
            System.err.println("GPC displayOutcome: " + errorMsg);
            if (sceneTextLabel != null) sceneTextLabel.setText(errorMsg);
            showChoicesArea(); // Still switch to choices area for terminal buttons
            if (choicesVBox != null) {
                 choicesVBox.getChildren().clear();
                 addTerminalOutcomeButtons();
            }
        }
    }

    private void addTerminalOutcomeButtons() {
        if (choicesVBox == null) {
            System.err.println("GPC addTerminalOutcomeButtons: choicesVBox is null!");
            return;
        }
        System.out.println("GPC: Adding terminal outcome buttons (Main Menu/Exit).");
        showChoicesArea(); // Ensure choices area is active
        choicesVBox.getChildren().clear();

        Button mainMenuButton = new Button("Return to Main Menu");
        mainMenuButton.getStyleClass().add("choice-button");
        mainMenuButton.setOnAction(event -> {
            try {
                if (audioManager != null) audioManager.playSoundEffect("/com/leave/engine/audio/clicker.wav");
                audioManager.stopBackgroundMusic(); // Good practice
                App.setRoot("gameEntry");
            } catch (IOException e) {
                System.err.println("GPC Error returning to main menu: " + e.getMessage());
                // Show error in a robust way if possible, even if sceneTextLabel isn't primary view
                Label errorLabel = new Label("Error returning to menu.");
                choicesVBox.getChildren().add(errorLabel);
            }
        });

        Button quitButton = new Button("Exit Game");
        quitButton.getStyleClass().add("choice-button");
        quitButton.setOnAction(event -> {
            System.out.println("GPC: Exit Game button clicked.");
            if (audioManager != null) {
                audioManager.playSoundEffect("/com/leave/engine/audio/clicker.wav"); // Play click before shutdown
                audioManager.shutdown();
            }
            Platform.exit();
            System.exit(0); // Force exit if Platform.exit() has issues
        });

        choicesVBox.getChildren().addAll(mainMenuButton, quitButton);
        choicesVBox.setDisable(false); // Make sure buttons are interactive
    }
  
}