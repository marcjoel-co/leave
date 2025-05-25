package com.leave.engine;

import java.io.IOException;

public class GameManager {

    private static GameManager instance; // For Singleton pattern

    private StoryLoader storyLoader;
    private String currentPlayerName = "Player"; // Default name
    
    private boolean gameOver = false;
    private String currentOutcomeId;
    private GameStory gameStory;
    private String currentSceneId;

    // Private constructor for Singleton
    private GameManager() {
        storyLoader = new StoryLoader();
    }

    // Public method to get the instance (Singleton)
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    /**
     * Loads the story from the specified resource path and prepares the game.
     * @param storyResourcePath The classpath resource path to the story JSON file.
     * @throws IOException If the story file cannot be loaded or parsed.
     */
    public void loadStory(String storyResourcePath) throws IOException {
        if (storyLoader == null) { 
            storyLoader = new StoryLoader();
        }
        this.gameStory = storyLoader.loadStory(storyResourcePath);
        if (this.gameStory == null || this.gameStory.getStartScene() == null) { // Use the public getter here
            throw new IOException("Story data or start scene is null after loading.");
        }
        System.out.println("Game story '" + this.getGameTitle() + "' loaded successfully."); // Use getter
    }


    /**
     * Starts or restarts the game.
     * Requires story to be loaded first via loadStory().
     * @param playerName The name of the player.
     */
    public void startGame() {
        // if (this.gameStory == null) {
        //     System.err.println("FATAL: Story not loaded. Call loadStory() before startGame().");
        //     return;
        // }
        // this.currentPlayerName = (playerName != null && !playerName.trim().isEmpty()) ? playerName : "Player";
        this.currentSceneId = this.gameStory.getStartScene(); // Accessing startScene via its public getter
        this.gameOver = false;
        this.currentOutcomeId = null;
        System.out.println("Game started. Player: " + this.currentPlayerName + ". Starting scene: " + this.currentSceneId);
        System.out.println("GameManager.startGame FINISHED. Player: " + this.currentPlayerName + 
                       ". currentSceneId SET TO: " + this.currentSceneId +
                       ". gameStory IS " + (this.gameStory != null ? "NOT NULL" : "NULL") +
                       ". gameStory.scenes IS " + (this.gameStory != null && this.gameStory.getScenes() != null ? "NOT NULL" : "NULL or gameStory is NULL"));

    }

    public void setCurrentPlayerCharacterName(String selectedCharacterName)
    {
        if (selectedCharacterName != null && !selectedCharacterName.trim().isEmpty()) {
            this.currentPlayerName = selectedCharacterName;
        } else {
            System.err.println("Invalid character name. Keeping default: " + this.currentPlayerName);
        }
    }
    /**
     * Gets the data for the current scene.
     * @return SceneData for the current scene, or null if not found or game not started.
     */
     /**
     * Gets the data for the current scene.
     * @return SceneData for the current scene, or null if not found or game not started.
     */
    public SceneData getCurrentSceneData() {
        if (gameStory == null || currentSceneId == null || gameStory.getScenes() == null) {
            System.err.println("GameManager: GameStory, currentSceneId, or scenes map is null.");
            return null;
        }
        SceneData sceneData = gameStory.getScenes().get(currentSceneId); 
        if (sceneData == null) {
            System.err.println("GameManager Error: Scene data for id '" + currentSceneId + "' is null (scene not found in map).");
        }
        return sceneData; // No cast needed because gameStory.getScenes() returns Map<String, SceneData>
    }
    /* 
     * Replaces placeholders in text (e.g., {playerName}).
     * @param rawText The text possibly containing placeholders.
     * @return The processed text.
     */
    public String processText(String rawText) {
        if (rawText == null) return "";
        String placeholder = gameStory != null && gameStory.getPlayerNamePlaceholder() != null ?
                             gameStory.getPlayerNamePlaceholder() : "{playerName}"; // Default placeholder
        return rawText.replace(placeholder, currentPlayerName);
    }

    /**
     * Processes a player's choice.
     * Updates the current scene or sets the game to an outcome state.
     * @param choice The ChoiceData object representing the player's selection.
     */
    public void makeChoice(ChoiceData choice) {
        if (choice == null || gameOver) {
            return;
        }
        System.out.println("Player chose: " + choice.getText());
        if (choice.getOutcome() != null) {
            setGameOver(choice.getOutcome());
        } else if (choice.getNextSceneId() != null) {
            advanceToScene(choice.getNextSceneId());
        } else {
            System.err.println("Choice has no next scene ID or outcome: " + choice.getText());
            // Potentially a dead-end in the story data or an error
        }
    }

    /**
     * Advances the game to the specified scene ID.
     * If the target scene has an immediate outcome, it will be processed.
     * @param sceneId The ID of the scene to transition to.
     */
    public void advanceToScene(String sceneId) {
        if (gameStory == null || gameStory.getScenes() == null || gameOver) {
            return;
        }
        // This will directly be SceneData if gameStory.getScenes() is correctly typed
        SceneData nextScene = gameStory.getScenes().get(sceneId); 
        if (nextScene != null) {
            this.currentSceneId = sceneId;
            System.out.println("Advanced to scene: " + this.currentSceneId);
            if (nextScene.getOutcome() != null) {
                setGameOver(nextScene.getOutcome());
            }
        } else {
            System.err.println("Cannot advance: Scene with ID '" + sceneId + "' not found.");
        }
    }

    /**
     * Sets the game to a game over state with a specific outcome.
     * @param outcomeId The ID of the outcome.
     */
    private void setGameOver(String outcomeId) {
        this.gameOver = true;
        this.currentOutcomeId = outcomeId;
        System.out.println("Game Over. Outcome: " + outcomeId);
        // The UI controller will then fetch outcome data to display.
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public OutcomeData getCurrentOutcomeData() {
        if (!gameOver || currentOutcomeId == null || gameStory == null || gameStory.getOutcomes() == null) {
            return null;
        }
        // This will directly be OutcomeData if gameStory.getOutcomes() is correctly typed
        return gameStory.getOutcomes().get(currentOutcomeId); 
    }


    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    // Potentially, a method to get an arbitrary scene by ID if needed elsewhere,
    // but getCurrentSceneData() is the primary one for gameplay flow.
    public SceneData getSceneDataById(String sceneId) {
        if (gameStory == null || gameStory.getScenes() == null) {
            return null;
        }
        
        return gameStory.getScenes().get(sceneId); 
    }
  /**
     * Gets the title of the loaded game story.
     * @return The game title, or null if the story isn't loaded.
     */
    public String getGameTitle() {
        return (gameStory != null) ? gameStory.getGameTitle() : null;
    }

    /**
     * Gets the ID of the starting scene from the loaded game story.
     * @return The start scene ID, or null if the story isn't loaded.
     */
    public String getStartSceneIdFromStory() { // Renamed to avoid confusion with currentSceneId
        return (gameStory != null) ? gameStory.getStartScene() : null;
    }

    /**
     * Gets the placeholder string used for player names in the story text.
     * @return The player name placeholder, or a default if not set or story not loaded.
     */
    public String getPlayerNamePlaceholderFromStory() {
        if (gameStory != null && gameStory.getPlayerNamePlaceholder() != null) {
            return gameStory.getPlayerNamePlaceholder();
        }
        return "{playerName}"; // Default if not specified in story
    }
}
    