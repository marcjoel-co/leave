package com.leave.engine;

public class OutcomeData {
    private String outcomeText;
    private String outcomeImagePath;
    private String outcomeAudioPath;

    public OutcomeData(String outcomeText, String outcomeImagePath, String outcomeAudioPath) {
        this.outcomeText = outcomeText;
        this.outcomeImagePath = outcomeImagePath;
        this.outcomeAudioPath = outcomeAudioPath;
    }

    public String getOutcomeText() {
        return outcomeText;
    }

    public String getOutcomeImagePath() {
        return outcomeImagePath;
    }

    public String getOutcomeAudioPath() {
        return outcomeAudioPath;
    }
    
}
