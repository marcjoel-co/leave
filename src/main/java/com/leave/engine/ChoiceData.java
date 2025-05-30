package com.leave.engine; // Or com.leave.engine

public class ChoiceData {
    private String text;
    private String nextSceneId;
    private String outcome;
    private String action; 
    // private String requiredItem; 
    private String requiredFlag; 

    // Getters and setters
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getNextSceneId() { return nextSceneId; }
    public void setNextSceneId(String nextSceneId) { this.nextSceneId = nextSceneId; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    // public String getRequiredItem() { return requiredItem; }
    // public void setRequiredItem(String requiredItem) { this.requiredItem = requiredItem; }
    public String getRequiredFlag() { return requiredFlag; }
    // public void setRequiredFlag(String requiredFlag) { this.requiredFlag = requiredFlag; }


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