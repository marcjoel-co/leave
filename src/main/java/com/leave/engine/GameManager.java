package com.leave.engine;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;



public class GameManager {
    private static GameManager instance;
    private StoryLoader storyLoader;
    private GameStory gameStory; // This should be GameStory with correctly typed Maps
    private String currentPlayerName = "Player";
    private String currentSceneId;
    private boolean gameOver = false;
    private String currentOutcomeId;
    private Set<String> playerInventory = new HashSet<>();
    private Set<String> storyFlags = new HashSet<>();
    private String currentPlayerPortraitPath;

    private GameManager() {
        storyLoader = new StoryLoader();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void loadStory(String storyResourcePath) throws IOException {
        if (storyLoader == null) storyLoader = new StoryLoader();
        this.gameStory = storyLoader.loadStory(storyResourcePath);
        if (this.gameStory == null || this.gameStory.getStartScene() == null) {
            throw new IOException("Story data or start scene is null after loading.");
        }
        System.out.println("Game story '" + getGameTitle() + "' loaded: " + storyResourcePath);
    }

    public void startGame() { 

        // test for valid
        if (this.gameStory == null) {
            System.err.println("FATAL ERROR in GameManager.startGame: Story not loaded.");
            throw new IllegalStateException("Game story must be loaded before starting game.");
        }
        
        String startSceneIDFromStory = this.gameStory.getStartScene();
        if (startSceneIDFromStory == null || startSceneIDFromStory.trim().isEmpty() ||
            this.gameStory.getScenes() == null || !this.gameStory.getScenes().containsKey(startSceneIDFromStory)) {
            System.err.println("FATAL ERROR in GameManager.startGame: Start scene ID '" + startSceneIDFromStory + 
                               "' is invalid or scenes map not populated correctly.");
            throw new IllegalStateException("Invalid start scene configuration in story data.");
        }
        this.currentSceneId = startSceneIDFromStory;
        this.gameOver = false;
        this.currentOutcomeId = null;
        this.currentPlayerPortraitPath = null;
        this.playerInventory.clear();
        this.storyFlags.clear();
        
        System.out.println("GameManager.startGame FINISHED. currentSceneId SET TO: " + this.currentSceneId +
                           ". currentPlayerName (current): '" + this.currentPlayerName + "'" +
                           ". gameStory IS " + (this.gameStory != null ? "NOT NULL" : "NULL") +
                           ". gameStory.scenes IS " + (this.gameStory.getScenes() != null ? "NOT NULL (Size: " + this.gameStory.getScenes().size() + ")" : "NULL"));
    }

    public void setCurrentPlayerCharacterName(String selectedCharacterName) {
       if (selectedCharacterName != null && !selectedCharacterName.trim().isEmpty()) {
            this.currentPlayerName = selectedCharacterName;
            System.out.println("GameManager: Player name set to: " + this.currentPlayerName);
        }
        else {
            System.err.println("GameManager: Invalid character name provided. Keeping: " + this.currentPlayerName);
        }
    }
    public void setCurrentPlayerPortraitPath(String path) 
    { 
        this.currentPlayerPortraitPath = path;
        System.out.println("GameManager: Player portrait path set to: " + path);
    }

     public String getCurrentPlayerPortraitPath() { 
        return this.currentPlayerPortraitPath;
    }

    


    public SceneData getCurrentSceneData() {
        if (gameStory == null || currentSceneId == null || gameStory.getScenes() == null) {
            System.err.println("GameManager.getCurrentSceneData: GameStory, currentSceneId, or scenes map is null.");
            return null;
        }
        SceneData sceneData = gameStory.getScenes().get(currentSceneId); 
        if (sceneData == null) {
            System.err.println("GameManager Error: Scene data for id '" + currentSceneId + "' is null (scene not found in map).");
        }
        return sceneData;
    }

    public String processText(String rawText) {
        if (rawText == null) return "";
        String placeholder = (gameStory != null && gameStory.getPlayerNamePlaceholder() != null) ?
                             gameStory.getPlayerNamePlaceholder() : "{playerName}";
        return rawText.replace(placeholder, currentPlayerName);
    }
    public void resetGameOver() {
    this.gameOver = false;
    // this.currentOutcomeId = null; // Also reset current outcome
    System.out.println("GameManager: Game over state reset.");
    }
    public String getCurrentSceneId() {
    return this.currentSceneId;
}

    public void makeChoice(ChoiceData choice) {
        if (choice == null || gameOver) {
            return;
        }
        System.out.println("Player chose: " + choice.getText());

        // Process any immediate action from the choice
        if (choice.getAction() != null && !choice.getAction().trim().isEmpty()) {
            processAction(choice.getAction());
        }

        if (choice.getOutcome() != null) {
            setGameOver(choice.getOutcome());
        } else if (choice.getNextSceneId() != null) {
            advanceToScene(choice.getNextSceneId());
        } else {
            System.err.println("Choice '" + choice.getText() + "' has no next scene ID or outcome.");
        }
    }

    public void advanceToScene(String sceneId) {
        if (sceneId == null || sceneId.trim().isEmpty()) {
            System.err.println("GameManager.advanceToScene: sceneId is null or empty.");
            return;
        }
        if (gameStory == null || gameStory.getScenes() == null || gameOver) {
            if (gameOver) System.out.println("GameManager.advanceToScene: Game is over, cannot advance.");
            else System.err.println("GameManager.advanceToScene: Story or scenes not loaded.");
            return;
        }
        
        SceneData nextScene = gameStory.getScenes().get(sceneId); 
        
        if (nextScene != null) { // nill check
            this.currentSceneId = sceneId;
            System.out.println("Advanced to scene: " + this.currentSceneId);

           
            if (nextScene.getAction() != null && !nextScene.getAction().trim().isEmpty()) {
                processAction(nextScene.getAction());
            }

            if (nextScene.getOutcome() != null) {
                setGameOver(nextScene.getOutcome());
            }
            // If it auto-transitions, the UI controller will call advanceToScene again.
        } else {
            System.err.println("Cannot advance: Scene with ID '" + sceneId + "' not found in story data.");
            // Optionally set a game over state for "broken story path"
            // setGameOver("ERROR_INVALID_SCENE_ID");
        }
    }

    /**
     * Processes generic actions defined in the story (e.g., ADD_ITEM_X, SET_FLAG_Y).
     * @param actionString The action string from the JSON.
     */
    public void processAction(String actionString) {
        if (actionString == null || actionString.trim().isEmpty()) return;
        System.out.println("GameManager: Processing action: " + actionString);
        
        // if (actionString.startsWith("ADD_ITEM_")) {
        //     String item = actionString.substring("ADD_ITEM_".length());
        //     addItemToInventory(item);
        // } else if (actionString.startsWith("SET_FLAG_")) {
        //     String flag = actionString.substring("SET_FLAG_".length());
        //     setFlag(flag);
        // }
       // heres were gonna implement our items if we have any
        // else {
        //     System.err.println("GameManager: Unknown action string: " + actionString);
        // }
    }

    // Changed from private to public so GamePlayController can directly set outcome from a scene object
    public void setGameOver(String outcomeId) {
        if (outcomeId == null || outcomeId.trim().isEmpty()) {
             System.err.println("GameManager.setGameOver: outcomeId is null or empty.");
             // Potentially set a default "ERROR_OUTCOME" if this happens
             this.currentOutcomeId = "ERROR_UNDEFINED_OUTCOME";
        } else {
            this.currentOutcomeId = outcomeId;
        }
        this.gameOver = true;
        System.out.println("Game Over. Outcome: " + this.currentOutcomeId);
    }
    
    public String getCurrentOutcomeId() {
    return this.currentOutcomeId;
}
    public boolean isGameOver() {
        return gameOver;
    }

    public OutcomeData getCurrentOutcomeData() {
        if (!gameOver || currentOutcomeId == null || gameStory == null || gameStory.getOutcomes() == null) {
            return null;
        }
        OutcomeData outcomeData = gameStory.getOutcomes().get(currentOutcomeId);
        if (outcomeData == null) {
            System.err.println("GameManager Error: Outcome data for id '" + currentOutcomeId + "' is null (outcome not found in map).");
        }
        return outcomeData;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public SceneData getSceneDataById(String sceneId) {
        if (gameStory == null || gameStory.getScenes() == null || sceneId == null) {
            return null;
        }
        SceneData sceneData = gameStory.getScenes().get(sceneId);
        if (sceneData == null) {
            System.err.println("GameManager Warning: Scene data for id '" + sceneId + "' requested but not found in map.");
        }
        return sceneData;
    }
  
    public String getGameTitle() {
        return (gameStory != null) ? gameStory.getGameTitle() : null;
    }

    public String getStartSceneIdFromStory() {
        return (gameStory != null) ? gameStory.getStartScene() : null;
    }

    public String getPlayerNamePlaceholderFromStory() {
        if (gameStory != null && gameStory.getPlayerNamePlaceholder() != null) {
            return gameStory.getPlayerNamePlaceholder();
        }
        return "{playerName}";
    }

     public boolean hasItem(String itemId){ return playerInventory.contains(itemId); }
     public boolean checkFlag(String flag){ return storyFlags.contains(flag); }
     public void addItemToInventory(String itemId) { this.playerInventory.add(itemId); System.out.println("Item added: " + itemId); }
     public void setFlag(String flag) { this.storyFlags.add(flag); System.out.println("Flag set: " + flag); }
}