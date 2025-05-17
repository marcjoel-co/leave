package com.leave.engine;

import java.util.List;

public class CharacterManager 
{
    private List<String> characters;
    private List<String> characterNames;
    private int currentIndex = 0;

    public CharacterManager(List<String> characters, List<String> characterNames)
    {
        this.characters = characters;
        this.characterNames = characterNames;
    }

    public void nextCharacter()
    {
        currentIndex = (currentIndex +1) % characters.size();
    }

    public String getImage()
    {
        return characters.get(currentIndex);
    }


    public String getCurrentName() 
    {
        return characterNames.get(currentIndex);
    }

    public int getCharacterIndex()
    {
        return currentIndex;
    }    

}
