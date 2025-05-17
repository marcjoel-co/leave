package com.leave.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leave.engine.utils.AnimationUtils;

import javafx.animation.Timeline; // Jackson import
import javafx.event.ActionEvent;   // Jackson import
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class SecondaryController {

    @FXML
    private Label dialogueLabel;

    @FXML
    private Button nextDialogueButton;

    @FXML
    private Button backToMenuButton;

    // Now loaded from JSON
    private List<String> dialogueLines = Collections.emptyList();
    private int currentDialogueIndex = 0;
    private String currentFullText;
    private ObjectMapper objectMapper = new ObjectMapper(); // Jackson's parser

    @FXML
    public void initialize() {
        loadDialogue("introduction"); // Load the "introduction" lines by default

        dialogueLabel.setOnMouseClicked(this::handleDialogueClick);

        if (nextDialogueButton != null) {
            nextDialogueButton.setDisable(true);
        }
        showNextDialogueLine();
    }

    private void loadDialogue(String dialogueKey) {
        try (InputStream is = getClass().getResourceAsStream("/com/leave/engine/data/sao.json")) {
            // if (is == null) {
            //     System.err.println("Cannot find dialogue.json!");
            //     dialogueLabel.setText("ERROR: Dialogue file not found.");
            //     dialogueLines = List.of("Error: Could not load dialogue. File missing.");
            //     return;
            // }
            Map<String, List<String>> allDialogues = objectMapper.readValue(is, new TypeReference<>() {});

            // test out if the JSON FILE DOES REALLY EXIST
            dialogueLines = allDialogues.getOrDefault(dialogueKey, List.of("Error: Dialogue key '" + dialogueKey + "' not found."));
            if (dialogueLines.isEmpty() || (dialogueLines.size() == 1 && dialogueLines.get(0).startsWith("Error:"))) {
                 System.err.println("Dialogue for key '" + dialogueKey + "' is missing or empty in JSON.");
                 if (dialogueLabel != null) dialogueLabel.setText("ERROR: No dialogue for key: " + dialogueKey);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load or parse dialogue.json: " + e.getMessage());
            dialogueLabel.setText("ERROR: Could not parse dialogue file.");
            dialogueLines = List.of("Error: Could not load dialogue due to file error.");
        }
        currentDialogueIndex = 0; // Reset index when loading new dialogue
    }

    private void showNextDialogueLine() {
        if (dialogueLines.isEmpty()) {
            if (dialogueLabel != null) dialogueLabel.setText("No dialogue to display.");
            if (nextDialogueButton != null) nextDialogueButton.setDisable(true);
            return;
        }
        if (currentDialogueIndex < dialogueLines.size()) {
            currentFullText = dialogueLines.get(currentDialogueIndex);
            if (nextDialogueButton != null) {
                nextDialogueButton.setDisable(true);
            }

            AnimationUtils.animateText(
                    dialogueLabel,
                    currentFullText,
                    () -> {
                        if (nextDialogueButton != null) {
                            nextDialogueButton.setDisable(false);
                        }
                    }
            );
            currentDialogueIndex++;
        } else {
            dialogueLabel.setText("[End of current dialogue sequence]");
            if (nextDialogueButton != null) {
                nextDialogueButton.setText("Continue");
                nextDialogueButton.setDisable(false);
                nextDialogueButton.setOnAction(event -> {
                    // TODO: Determine next action: load different dialogue, go to game, etc.
                    System.out.println("End of dialogue sequence reached. Implement next step.");
                    // Example: Go back to menu, or load another set of dialogues
                    loadDialogue("exampleScene2");
                    showNextDialogueLine();
            
            
                    // try {
                    //     App.setRoot("mainMenu");
                    // } catch (IOException e) {
                    //     e.printStackTrace();
                    // }
                });
            }
        }
    }

    @FXML
    private void handleNextDialogue(ActionEvent event) {
        Object timelineObj = dialogueLabel.getProperties().get("activeTextAnimation");
        if (timelineObj instanceof Timeline && ((Timeline) timelineObj).getStatus() == Timeline.Status.RUNNING) {
            AnimationUtils.completeTextAnimation(dialogueLabel);
            if (nextDialogueButton != null) {
                 nextDialogueButton.setDisable(false);
            }
        } else {
            showNextDialogueLine();
        }
    }

    private void handleDialogueClick(MouseEvent event) {
        Object timelineObj = dialogueLabel.getProperties().get("activeTextAnimation");
        if (timelineObj instanceof Timeline && ((Timeline) timelineObj).getStatus() == Timeline.Status.RUNNING) {
            AnimationUtils.completeTextAnimation(dialogueLabel);
            if (nextDialogueButton != null) {
                 nextDialogueButton.setDisable(false);
            }
        } else {
             if (currentDialogueIndex <= dialogueLines.size() && !dialogueLines.isEmpty()) {
                showNextDialogueLine();
            }
        }
    }


    @FXML
    private void handleBackToMenu(ActionEvent event) {
        try {
            App.setRoot("mainMenu");
        } catch (IOException e) {
            System.err.println("Error returning to main menu:");
            e.printStackTrace();
        }
    }
}