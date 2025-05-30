package com.leave.engine; 

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
public class SceneData 
{
    private String id;                      
    private List<DialogueEntry> dialogue;   
    private String backgroundImage;         // Path for a static background image
    private SpriteInfo backgroundSprite;    // Information for an animated background sprite
    private CharacterSpriteInfo characterSprite; 
    private List<InteractiveObjectInfo> objects; 
    private List<ChoiceData> choices;       
    private String autoTransitionTo;        
    private String outcome;                 
    private String backgroundMusic;         
    private String action;                  // Currently unused 
    private String endingTitle; // tite for the ending scene, 
    

    @JsonProperty("nextSceneIdIfNameSet") 
    private String nextSceneIdIfNameSet; 

    
    public SceneData() {}

    // --- Getters and Setters for ALL fields ---
    // Jackson needs these (or public fields) to map JSON properties to Java object fields.

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<DialogueEntry> getDialogue() { return dialogue; }
    public void setDialogue(List<DialogueEntry> dialogue) { this.dialogue = dialogue; }

    public String getBackgroundImage() { return backgroundImage; }
    public void setBackgroundImage(String backgroundImage) { this.backgroundImage = backgroundImage; }

    public SpriteInfo getBackgroundSprite() { return backgroundSprite; }
    public void setBackgroundSprite(SpriteInfo backgroundSprite) { this.backgroundSprite = backgroundSprite; }

    public CharacterSpriteInfo getCharacterSprite() { return characterSprite; }
    public void setCharacterSprite(CharacterSpriteInfo characterSprite) { this.characterSprite = characterSprite; }
    
    public List<InteractiveObjectInfo> getObjects() { return objects; }
    public void setObjects(List<InteractiveObjectInfo> objects) { this.objects = objects; }

    public List<ChoiceData> getChoices() { return choices; }
    public void setChoices(List<ChoiceData> choices) { this.choices = choices; }

    public String getAutoTransitionTo() { return autoTransitionTo; }
    public void setAutoTransitionTo(String autoTransitionTo) { this.autoTransitionTo = autoTransitionTo; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    
    public String getBackgroundMusic() { return backgroundMusic; }
    public void setBackgroundMusic(String backgroundMusic) { this.backgroundMusic = backgroundMusic; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNextSceneIdIfNameSet() { 
        return nextSceneIdIfNameSet;
    }

    public void setNextSceneIdIfNameSet(String nextSceneIdIfNameSet) { 
        this.nextSceneIdIfNameSet = nextSceneIdIfNameSet;
    }

    public String getEndingTitle() 
    {
    return endingTitle;
    }

    public void setEndingTitle(String endingTitle) 
    
    {
    this.endingTitle = endingTitle;
    }
}