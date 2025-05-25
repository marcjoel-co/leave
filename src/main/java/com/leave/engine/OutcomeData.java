package com.leave.engine;

import com.fasterxml.jackson.annotation.JsonProperty; // Good for explicit mapping

public class OutcomeData {
    @JsonProperty("message") // Explicitly map JSON key "message" to this field
    private String message;

    @JsonProperty("nextSceneId") // Explicitly map JSON key "nextSceneId" to this field
    private String nextSceneId;

    // Default constructor needed by Jackson for deserialization
    public OutcomeData() {}

    // Getters
    public String getMessage() {
        return message;
    }

    public String getNextSceneId() {
        return nextSceneId;
    }

    // Setters (also often needed by Jackson unless fields are public or using other annotations)
    public void setMessage(String message) {
        this.message = message;
    }

    public void setNextSceneId(String nextSceneId) {
        this.nextSceneId = nextSceneId;
    }

    // Optional: toString for debugging
    @Override
    public String toString() {
        return "OutcomeData{" +
               "message='" + message + '\'' +
               ", nextSceneId='" + nextSceneId + '\'' +
               '}';
    }
}