package com.leave.engine;

//A class for managing choices of player
public class ChoiceData {

    //private variables for dialogues, scenes and gameOver outcome
    private String text;
    private String nextSceneId;
    private String outcome; 

    //gets the choice of the player
    public String getText() {
        return text;
    }

    //sets the choice of the player to the private variable
    public void setText(String text) {
        this.text = text;
    }

    //gets the response of player to the next scene
    public String getNextSceneId() {
        return nextSceneId;
    }

    //sets next scene that responded of the player
    public void setNextSceneId(String nextSceneId) {
        this.nextSceneId = nextSceneId;
    }

    //gets the of the decision of the player
    public String getOutcome() {
        return outcome;
    }

    //sets the decision of the player and determine outcome
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}