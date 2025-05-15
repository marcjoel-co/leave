package com.leave.engine;

// ... other imports ...
import static com.leave.engine.utils.AnimationUtils.*; // Your existing static import

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException; // Make sure this is imported
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class MainMenuController {

    @FXML private Label characterNameLabel;
    @FXML private ImageView characterImageView;
    @FXML private Circle backgroundSpotlightCircle;
    @FXML private Button characterChangeButton;
    @FXML private Button newGameButton;
    @FXML private Button loadGameButton;
    @FXML private VBox menuButtonBox;
    @FXML private VBox centerContentVBox;
    @FXML private Label tradeMarc;

    private List<String> characterImageFiles;
    private List<String> characterNames;
    private int currentCharIndex = 0;
    private boolean isAnimating = false;
    private List<Node> contentNodesToAnimateExcludingSpotlight;

    @FXML
    public void initialize() {
        characterImageFiles = new ArrayList<>(Arrays.asList(
                "images/characters/character1.png", "images/characters/character2.png",
                "images/characters/character3.png", "images/characters/character4.png",
                "images/characters/character5.png"
        ));

        if (newGameButton == null) {
            System.err.println("CRITICAL: newGameButton is NULL in initialize()!");
        } else {
            System.out.println("SUCCESS: newGameButton was injected. Current disabled state: " + newGameButton.isDisabled());
        }
        characterNames = new ArrayList<>(Arrays.asList(
                "Lynx", "Shadow", "Aurora",
                "Gale", "Terra" // Example names
        ));

        contentNodesToAnimateExcludingSpotlight = new ArrayList<>();
        if (menuButtonBox != null) contentNodesToAnimateExcludingSpotlight.add(menuButtonBox);
        else System.err.println("MainMenuController: menuButtonBox is null.");

        // Ensure centerContentVBox itself isn't null before trying to get children.
        // For now, let's assume characterImageView and characterNameLabel are top-level for content to fade,
        // rather than assuming they are children of centerContentVBox for this animation logic.
        // You were adding centerContentVBox, not its children previously for the main fade.
        // If you intended specific children of centerContentVBox, that logic would need adjusting.
        // Sticking to your previous structure:
        if (centerContentVBox != null) contentNodesToAnimateExcludingSpotlight.add(centerContentVBox);
        else System.err.println("MainMenuController: centerContentVBox is null for content list.");

        if (tradeMarc != null) contentNodesToAnimateExcludingSpotlight.add(tradeMarc);
        else System.err.println("MainMenuController: tradeMarc is null.");


        if (characterImageFiles.isEmpty() || characterNames.isEmpty() || characterImageFiles.size() != characterNames.size()) {
            System.err.println("Character data is not configured correctly.");
            if(characterNameLabel != null) characterNameLabel.setText("Character data error");
            return;
        }
        loadCharacterData(currentCharIndex, true);
    }

    private void loadCharacterData(int index, boolean isInitialLoad) {
        if (index < 0 || index >= characterImageFiles.size()) return;
        if (characterImageView == null || characterNameLabel == null) {
            System.err.println("loadCharacterData: characterImageView or characterNameLabel is null.");
            return;
        }

        String imagePath = characterImageFiles.get(index);
        Image charImage = null;
        try {
            // Ensure path is relative to the resources folder as before
            // Assuming images folder is directly under com/leave/engine/resources/
            charImage = new Image(getClass().getResourceAsStream("/com/leave/engine/" + imagePath));
            if (charImage == null || charImage.isError()) {
                System.err.println("Error loading image: " + imagePath);
                if (charImage != null && charImage.getException() != null) {
                    charImage.getException().printStackTrace();
                }
                characterImageView.setImage(null); // Clear image on error
            } else {
                characterImageView.setImage(charImage);
            }
        } catch (Exception e) {
            System.err.println("Exception loading image: " + imagePath);
            e.printStackTrace();
            characterImageView.setImage(null);
        }

        characterNameLabel.setText("You are playing as " + characterNames.get(index));

        if (isInitialLoad) {
            // Set initial opacities
            if (contentNodesToAnimateExcludingSpotlight != null) {
                for (Node node : contentNodesToAnimateExcludingSpotlight) {
                    if (node != null) node.setOpacity(1.0);
                }
            }
            if (backgroundSpotlightCircle != null) backgroundSpotlightCircle.setOpacity(1.0);
            characterImageView.setOpacity(1.0);
            characterNameLabel.setOpacity(1.0);
        }
    }


    @FXML
    private void handleCharacterChange() {
        if (isAnimating || characterImageFiles.size() <= 1) return;
        isAnimating = true;

        // if (characterChangeButton != null) characterChangeButton.setDisable(true);
        // // if (newGameButton != null) newGameButton.setDisable(true);
        // if (loadGameButton != null) loadGameButton.setDisable(true);

        // Safety check: ensure essential nodes for animation are not null
        boolean essentialNodesMissing = backgroundSpotlightCircle == null ||
                                   characterImageView == null ||
                                   characterNameLabel == null ||
                                   contentNodesToAnimateExcludingSpotlight == null ||
                                   contentNodesToAnimateExcludingSpotlight.stream().anyMatch(Objects::isNull) ||
                                   contentNodesToAnimateExcludingSpotlight.isEmpty();


        if (essentialNodesMissing) {
            System.err.println("MainMenuController: Essential nodes for character change animation are null or list is empty.");
            isAnimating = false;
            if (characterChangeButton != null) characterChangeButton.setDisable(false);
            if (newGameButton != null) newGameButton.setDisable(false);
            if (loadGameButton != null) loadGameButton.setDisable(false);
            return;
        }


        Duration fadeDuration = Duration.millis(300);
        Duration shortPauseDuration = Duration.millis(100);
        Duration contentFadeDuration = Duration.millis(250);
        Duration blinkSegmentDuration = Duration.millis(100);

        // --- FADE OUT SEQUENCE ---
        FadeTransition spotlightFadeOut = createFadeTransition(backgroundSpotlightCircle, fadeDuration, backgroundSpotlightCircle.getOpacity(), 0.0);
        ParallelTransition otherContentFadeOut = new ParallelTransition();
        for (Node node : contentNodesToAnimateExcludingSpotlight) {
             // Make sure the node itself is not null before creating transition
            if (node != null) {
                otherContentFadeOut.getChildren().add(
                    createFadeTransition(node, contentFadeDuration, node.getOpacity(), 0.0)
                );
            }
        }
        // Also fade character details with other content
        if (characterImageView != null) {
             otherContentFadeOut.getChildren().add(
                createFadeTransition(characterImageView, contentFadeDuration, characterImageView.getOpacity(), 0.0)
            );
        }
        if (characterNameLabel != null) {
             otherContentFadeOut.getChildren().add(
                createFadeTransition(characterNameLabel, contentFadeDuration, characterNameLabel.getOpacity(), 0.0)
            );
        }


        SequentialTransition fullFadeOutSequence = new SequentialTransition(
            spotlightFadeOut,
            createPauseTransition(shortPauseDuration),
            otherContentFadeOut
        );

        fullFadeOutSequence.setOnFinished(fadeOutEvent -> {
            currentCharIndex = (currentCharIndex + 1) % characterImageFiles.size();
            if(characterImageView != null) characterImageView.setOpacity(0.0);
            if(characterNameLabel != null) characterNameLabel.setOpacity(0.0);
            loadCharacterData(currentCharIndex, false); // Load new data, don't set initial opacity here

            // --- FADE IN / BLINK IN SEQUENCE ---
            Duration mainPauseDuration = Duration.millis(150);
            Timeline spotlightBlinkIn = createBlinkTimeline(backgroundSpotlightCircle, blinkSegmentDuration, 1.0);
            ParallelTransition otherContentBlinkIn = new ParallelTransition();
            for (Node node : contentNodesToAnimateExcludingSpotlight) {
                 if (node != null) {
                    otherContentBlinkIn.getChildren().add(
                        createBlinkTimeline(node, blinkSegmentDuration.add(Duration.millis(20)), 1.0)
                    );
                }
            }
            Timeline charImageBlinkIn = createBlinkTimeline(characterImageView, blinkSegmentDuration, 1.0);
            FadeTransition charNameFadeIn = createFadeTransition(characterNameLabel, blinkSegmentDuration.multiply(5), 0.0, 1.0);

            SequentialTransition showNewCharacterSequence = new SequentialTransition(
                createPauseTransition(mainPauseDuration),
                new ParallelTransition(spotlightBlinkIn, otherContentBlinkIn, charImageBlinkIn, charNameFadeIn)
            );

            showNewCharacterSequence.play();
            showNewCharacterSequence.setOnFinished(fadeInEvent -> {
                isAnimating = false;
                if (characterChangeButton != null) characterChangeButton.setDisable(false);
                if (newGameButton != null) newGameButton.setDisable(false);
                if (loadGameButton != null) loadGameButton.setDisable(false);
            });
        });

        fullFadeOutSequence.play();
    }

    @FXML
    public void handleNewGame(ActionEvent event) {
        System.out.println("handleNewGame in MainMenuController called! Transitioning to dialogue...");
        try {
            // TODO: Before switching, you might want to play a short fade-out animation
            // for the main menu elements if desired. For now, direct switch.
            App.setRoot("secondary"); // This will load secondary.fxml
        } catch (IOException e) {
            System.err.println("Error loading secondary.fxml for new game:");
            e.printStackTrace();
            // TODO: Show an error dialog to the user
        }
    }

    @FXML
    public void handleLoadGame(ActionEvent event) {
        System.out.println("handleLoadGame (Quit) in MainMenuController called!");
        javafx.application.Platform.exit();
    }
}