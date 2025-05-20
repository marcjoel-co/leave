package com.leave.engine;

import java.util.Map;

public class GameStory {
    
    /*
     * Private variables for game title, startScene and the place holder
     * of unique player name
     */
    private String gameTitle;
    private String startScene;
    private String playerNamePlaceholder;

    private Map<String, SceneData> scenes; // Key is sceneId
    private Map<String, OutcomeData> outcomes; // Key is outcomeId
    // Getters and setters (or make fields public if simple, though getters/setters are better practice)
}
    

