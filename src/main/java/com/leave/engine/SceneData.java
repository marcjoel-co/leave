package com.leave.engine; // Or com.leave.engine

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SceneData {
    @JsonProperty("id")
    private String id; // Though optional if key is used

    @JsonProperty("text")
    private String text;

    @JsonProperty("backgroundImage")
    private String backgroundImage;

    @JsonProperty("choices")
    private List<ChoiceData> choices; // Ensure this is correctly typed

    @JsonProperty("autoTransitionTo")
    private String autoTransitionTo;

    @JsonProperty("outcome")
    private String outcome;

    // Default constructor for Jackson
    public SceneData() {}

    // Getters
    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public List<ChoiceData> getChoices() { // Must return List<ChoiceData>
        return choices;
    }

    public String getAutoTransitionTo() {
        return autoTransitionTo;
    }

    public String getOutcome() {
        return outcome;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public void setChoices(List<ChoiceData> choices) { // Must accept List<ChoiceData>
        this.choices = choices;
    }

    public void setAutoTransitionTo(String autoTransitionTo) {
        this.autoTransitionTo = autoTransitionTo;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    
}