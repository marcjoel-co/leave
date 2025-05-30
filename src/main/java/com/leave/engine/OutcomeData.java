package com.leave.engine;

import com.fasterxml.jackson.annotation.JsonProperty; 

public class OutcomeData {
    @JsonProperty("message") 
    private String message;

    @JsonProperty("nextSceneId") 
    private String nextSceneId;

    
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