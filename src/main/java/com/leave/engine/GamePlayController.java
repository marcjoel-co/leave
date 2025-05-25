package com.leave.engine;

import static com.leave.engine.utils.AnimationUtils.animateText; // Assuming static import

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.application.Platform; // For runLater if needed

public class GamePlayController implements Initializable {

    @FXML private ImageView sceneBackgroundImageView;
    @FXML private Label sceneTextLabel;
    @FXML private VBox choicesVBox;

    private GameManager gameManager;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.gameManager = GameManager.getInstance(); // Get the singleton instance
        displayCurrentScene();
    }

    public void displayCurrentScene() {
        if (gameManager.isGameOver()) {
            displayOutcome();
            return;
        }

        SceneData currentScene = gameManager.getCurrentSceneData();
        if (currentScene == null) {
            System.err.println("GamePlayController: No current scene data found!");
            sceneTextLabel.setText("Error: Story sequence broken.");
            choicesVBox.getChildren().clear();
            return;
        }

        // 1. Set Background (Handle null or default)
        if (currentScene.getBackgroundImage() != null && !currentScene.getBackgroundImage().isEmpty()) {
            try {
                // Assuming image paths in JSON are like "images/backgrounds/forest.png"
                // and need to be prefixed for classpath resource loading.
                String fullBgPath = "/com/leave/engine/" + currentScene.getBackgroundImage();
                URL bgUrl = getClass().getResource(fullBgPath);
                if (bgUrl != null) {
                    sceneBackgroundImageView.setImage(new Image(bgUrl.toExternalForm()));
                } else {
                    System.err.println("Background image not found: " + fullBgPath);
                    sceneBackgroundImageView.setImage(null); // Clear if not found
                }
            } catch (Exception e) {
                System.err.println("Error loading background image: " + currentScene.getBackgroundImage());
                e.printStackTrace();
                sceneBackgroundImageView.setImage(null);
            }
        } else {
            sceneBackgroundImageView.setImage(null); // No background specified for this scene
        }

        // 2. Display Scene Text (using your text animator)
        String processedText = gameManager.processText(currentScene.getText());
        animateText(sceneTextLabel, processedText, 30, () -> { // 30ms delay, onFinished (lambda)
            // This lambda is called when animateText finishes.
            // Now, display choices or handle auto-transition if text animation is done.
            Platform.runLater(() -> { // Ensure UI updates are on JavaFX Application Thread
                if (currentScene.getAutoTransitionTo() != null) {
                    gameManager.advanceToScene(currentScene.getAutoTransitionTo());
                    displayCurrentScene(); // Recursively call to display the new auto-transitioned scene
                } else if (currentScene.getOutcome() != null) {
                     gameManager.advanceToScene(currentScene.getId()); // This sets outcome flags in GM
                     displayOutcome();
                }
                // Choices are displayed *after* text animation (handled by the animateText callback)
                // OR displayed immediately if auto-transition handles things before choices appear.
                // For simplicity now, choices will appear *after* text finishes and no auto-transition occurs.
                else if (currentScene.getChoices() != null && !currentScene.getChoices().isEmpty()){
                     populateChoices(currentScene);
                }
            });
        });


        // 3. Display Choices (initially populateChoices was here, moved to animateText callback)
        // If there's no text animation, or you want choices visible during animation,
        // you might call populateChoices directly here instead of in the callback.
        // For now, let's keep it after text is done.
        if (currentScene.getChoices() == null || currentScene.getChoices().isEmpty()) {
             choicesVBox.getChildren().clear(); // No choices for this scene
             // If no choices AND no auto-transition AND no outcome, it's a story end-point or needs a "Continue" button.
        }
        // Populate choices is now called from animateText's onFinished if applicable
    }
    
    private void populateChoices(SceneData sceneData) {
        choicesVBox.getChildren().clear(); // Clear previous choices
        if (sceneData.getChoices() != null) {
            for (ChoiceData choice : sceneData.getChoices()) {
                Button choiceButton = new Button(gameManager.processText(choice.getText()));
                choiceButton.getStyleClass().add("dialogue-button"); // Use style from your CSS
                choiceButton.setOnAction(event -> handleChoiceSelected(choice));
                choicesVBox.getChildren().add(choiceButton);
            }
        }
    }

    private void handleChoiceSelected(ChoiceData choice) {
        choicesVBox.setDisable(true); // Prevent multiple clicks while processing
        gameManager.makeChoice(choice);
        // After making choice, redisplay (which will show new scene or outcome)
        // Run on Platform.runLater to ensure any state changes in GameManager are processed
        // before trying to update UI from potentially different thread if choice had complex logic
        Platform.runLater(() -> {
            displayCurrentScene();
            choicesVBox.setDisable(false); // Re-enable for next set of choices
        });
    }

    private void displayOutcome() {
        OutcomeData outcome = gameManager.getCurrentOutcomeData();
        if (outcome != null) {
            sceneTextLabel.setText(gameManager.processText(outcome.getMessage())); // Use animateText for outcome too if desired
            choicesVBox.getChildren().clear(); // No more choices
            
            if (outcome.getNextSceneId() != null) {
                // E.g., an "outro" scene
                Button continueButton = new Button("Continue");
                continueButton.getStyleClass().add("dialogue-button");
                continueButton.setOnAction(e -> {
                    gameManager.advanceToScene(outcome.getNextSceneId());
                    displayCurrentScene(); // Display the "outro" scene
                });
                choicesVBox.getChildren().add(continueButton);
            } else {
                // True game end, maybe show a "Game Over" or "Play Again?" button
                Button endButton = new Button("End Game");
                endButton.getStyleClass().add("dialogue-button");
                endButton.setOnAction(e -> Platform.exit());
                choicesVBox.getChildren().add(endButton);
            }
        } else {
            sceneTextLabel.setText("Game Over. (Outcome data missing)");
        }
        // Clear background or set specific outcome background
        sceneBackgroundImageView.setImage(null); // Example: clear background for outcome screen
    }
}