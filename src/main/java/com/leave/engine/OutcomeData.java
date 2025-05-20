package com.leave.engine;

//A class for managing Outcomes of the dicisions of players 
public class OutcomeData {
    private String outcomeText;
    private String outcomeImagePath;
    private String outcomeAudioPath;

    //A method for displaying the outcome of the player
    public OutcomeData(String outcomeText, String outcomeImagePath, String outcomeAudioPath) {
        this.outcomeText = outcomeText;
        this.outcomeImagePath = outcomeImagePath;
        this.outcomeAudioPath = outcomeAudioPath;
    }

    //gets the outcome of player
    public String getOutcomeText() {
        return outcomeText;
    }

    //gets a specifed image to display along with outcome
    public String getOutcomeImagePath() {
        return outcomeImagePath;
    }

    //gets a specified audio to play during the display of outcome 
    public String getOutcomeAudioPath() {
        return outcomeAudioPath;
    }
    
}
