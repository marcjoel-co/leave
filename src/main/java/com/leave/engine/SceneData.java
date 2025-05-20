package com.leave.engine;

import java.util.List;

public class SceneData {
    private String id; // Can be useful, though often it's the map key
    private String text;
    private String backgroundImage; // Path to image
    private List<ChoiceData> choices;
    private String autoTransitionTo; // sceneId
    private String outcome; 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public List getChoices() {
        return choices;
    }

    public void setChoices(List choices) {
        this.choices = choices;
    }

    public String getAutoTransitionTo() {
        return autoTransitionTo;
    }

    public void setAutoTransitionTo(String autoTransitionTo) {
        this.autoTransitionTo = autoTransitionTo;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
    
}