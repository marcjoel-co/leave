package com.leave.engine; // Or com.leave.engine

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChoiceData {
    @JsonProperty("text")
    private String text;

    @JsonProperty("nextSceneId")
    private String nextSceneId;

    @JsonProperty("outcome")
    private String outcome; 

    // Default constructor for Jackson
    public ChoiceData() {}

    // Getters
    public String getText() {
        return text;
    }

    public String getNextSceneId() {
        return nextSceneId;
    }

    public String getOutcome() {
        return outcome;
    }

    // Setters
    public void setText(String text) {
        this.text = text;
    }

    public void setNextSceneId(String nextSceneId) {
        this.nextSceneId = nextSceneId;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    // Optional: toString for debugging
    @Override
    public String toString() {
        return "ChoiceData{" +
               "text='" + text + '\'' +
               ", nextSceneId='" + nextSceneId + '\'' +
               ", outcome='" + outcome + '\'' +
               '}';
    }
}