package com.leave.engine;

import java.util.List;

public class SceneData {
    private String id; // Can be useful, though often it's the map key
    private String text; // A text to display along with a scene
    private String backgroundImage; // Path to image
    private List<ChoiceData> choices; // An Array-list of existing player choices
    private String autoTransitionTo; // sceneId
    private String outcome; 

    //gets the ID of the current scene
    public String getId() {
        return id;
    }

    //sets the ID of the current scene
    public void setId(String id) {
        this.id = id;
    }

    //gets the displayed text
    public String getText() {
        return text;
    }

    //sets the displayed text
    public void setText(String text) {
        this.text = text;
    }
    
    //gets the file path of the background image
    public String getBackgroundImage() {
        return backgroundImage;
    }

    //sets the file path of the background image
    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    //A public method to get the list of choices
    public List getChoices() {
        return choices;
    }

    //sets the choice to display on screen
    public void setChoices(List choices) {
        this.choices = choices;
    }

    //gets the value of the auto transitions
    public String getAutoTransitionTo() {
        return autoTransitionTo;
    }

    //sets the value of the auto transition
    public void setAutoTransitionTo(String autoTransitionTo) {
        this.autoTransitionTo = autoTransitionTo;
    }

    //gets the outcome of player decision
    public String getOutcome() {
        return outcome;
    }

    //sets the value of the outcome
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
    
}