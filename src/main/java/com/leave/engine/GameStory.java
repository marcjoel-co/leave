package com.leave.engine;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GameStory {
    @JsonProperty("gameTitle") 
    private String gameTitle;

    @JsonProperty("startScene")
    private String startScene;

    @JsonProperty("playerNamePlaceholder")
    private String playerNamePlaceholder;

    @JsonProperty("scenes")
    private Map<String, SceneData> scenes; 

    
    public Map<String, SceneData> getScenes() {
        return scenes;
    }

    public void setScenes(Map<String, SceneData> scenes) {
        this.scenes = scenes;
    }

    @JsonProperty("outcomes")
    private Map<String, OutcomeData> outcomes;

    // --- Default constructor (needed by Jackson) ---
    public GameStory() {}
    
    
    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public String getStartScene() { return startScene; }
    public void setStartScene(String startScene) { this.startScene = startScene; }

    public String getPlayerNamePlaceholder() { return playerNamePlaceholder; }
    public void setPlayerNamePlaceholder(String playerNamePlaceholder) { this.playerNamePlaceholder = playerNamePlaceholder; }

    // public Map<String, Object> getScenes() {  return scenes; }
    // public void setScenes(Map<String, SceneData> scenes) { this.scenes = scenes; }

    public Map<String, OutcomeData> getOutcomes() { return outcomes; }
    public void setOutcomes(Map<String, OutcomeData> outcomes) { this.outcomes = outcomes; }
}