package com.leave.engine;
import java.util.List;

//a class in managing character loading
public class CharacterManager {

    /*
     * private variables in storing image files, character name
     * and the index of each character
     */
    private final List<String> imageFiles; 
    private final List<String> characterNames;
    private int currentIndex = 0;

    /*
     * Manages the list of characters if they are existing or not
     */
    public CharacterManager(List<String> imageFilePaths, List<String> names) { 
        if (imageFilePaths == null || names == null || imageFilePaths.isEmpty() || names.isEmpty() || imageFilePaths.size() != names.size()) {
            throw new IllegalArgumentException(
                "Character data lists cannot be null/empty and must have the same size. " +
                "ImageFiles.size=" + (imageFilePaths != null ? imageFilePaths.size() : "null") +
                ", Names.size=" + (names != null ? names.size() : "null")
            );
        }
        // Use List.copyOf to make immutable copies, protecting against external modification
        this.imageFiles = List.copyOf(imageFilePaths);
        this.characterNames = List.copyOf(names);
    }

    //A method to switch through the next character
    public void nextCharacter() {
        if (imageFiles.isEmpty()) return;
        currentIndex = (currentIndex + 1) % imageFiles.size();
    }

    //A method for switching to the previous character
    // public void previousCharacter() {
    //     if (imageFiles.isEmpty()) return;
    //     currentIndex = (currentIndex - 1 + imageFiles.size()) % imageFiles.size(); // 
    // }

    // get the image path of a character
    public String getCurrentImagePath() {
        if (imageFiles.isEmpty()) return null;
        return imageFiles.get(currentIndex);
    }


    //Gets the name of the current character
    public String getCurrentName() {
        if (characterNames.isEmpty()) return null; 
        return characterNames.get(currentIndex);
    }

    //gets returns the next frame to display
    public int getCurrentIndex() {
        return currentIndex;
    }

    //A pause method to stay on said frame
    public void setCurrentIndex(int index) {
        if (index >= 0 && index < imageFiles.size()) {
            this.currentIndex = index;
        } else {
            System.err.println("CharacterManager: Attempt to set invalid current index: " + index);
           
        }
    }

    //gets the number of characters
    public int getCharacterCount() {
        return imageFiles.size();
    }
    
}