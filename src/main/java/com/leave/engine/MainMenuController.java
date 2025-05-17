package com.leave.engine;


import java.io.IOException; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.leave.engine.utils.AnimationUtils.createBlinkTimeline;
import static com.leave.engine.utils.AnimationUtils.createFadeTransition;
import static com.leave.engine.utils.AnimationUtils.createPauseTransition;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label; // Make sure this is imported
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;


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
    private List<Node> contentNodewoSpotlight;

    @FXML
    public void initialize()
     {
        List<String> characterImageFiles = new ArrayList<>(Arrays.asList(
        "images/characters/character1.png", "images/characters/character2.png",
        "images/characters/character3.png", "images/characters/character4.png",
        "images/characters/character5.png", "images/characters/character5.png" 
        ));

        List<String> characterNames = new ArrayList<>(Arrays.asList(
            "Pocholo", "Eijel", "MARC",
            "Hera", "Denver", "Louise (sir)"
            
        ));
        if (newGameButton == null) {
            System.err.println("CRITICAL: newGameButton is NULL in initialize()!");
        } else {
            System.out.println("SUCCESS: newGameButton was injected. Current disabled state: " + newGameButton.isDisabled());
        }
        characterNames = new ArrayList<>(Arrays.asList(
                "Pocholo", "Eijel", "MARC",
                "Hera", "Denver", "Louise (sir)"
        ));

        contentNodewoSpotlight = new ArrayList<>();
        if (menuButtonBox != null) contentNodewoSpotlight.add(menuButtonBox);
        else System.err.println("MainMenuController: menuButtonBox is null.");

        
        if (centerContentVBox != null) contentNodewoSpotlight.add(centerContentVBox);
        else System.err.println("MainMenuController: centerContentVBox is null for content list.");

        if (tradeMarc != null) contentNodewoSpotlight.add(tradeMarc);
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
                characterImageView.setImage(null); 
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
            if (contentNodewoSpotlight != null) {
                for (Node node : contentNodewoSpotlight) {
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
                                   contentNodewoSpotlight == null ||
                                   contentNodewoSpotlight.stream().anyMatch(Objects::isNull) ||
                                   contentNodewoSpotlight.isEmpty();


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

       
        FadeTransition spotlightFadeOut = createFadeTransition(backgroundSpotlightCircle, fadeDuration, backgroundSpotlightCircle.getOpacity(), 0.0);
        ParallelTransition otherContentFadeOut = new ParallelTransition();
        for (Node node : contentNodewoSpotlight) {
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

        
            Duration mainPauseDuration = Duration.millis(150);
            Timeline spotlightBlinkIn = createBlinkTimeline(backgroundSpotlightCircle, blinkSegmentDuration, 1.0);
            ParallelTransition otherContentBlinkIn = new ParallelTransition();
            for (Node node : contentNodewoSpotlight) {
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