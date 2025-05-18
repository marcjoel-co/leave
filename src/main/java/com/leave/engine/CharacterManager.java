package com.leave.engine;

import java.util.List;
// No need for java.io.IOException or javafx.* imports here (unless you move image loading later)

public class CharacterManager {
    private final List<String> imageFiles; // Renamed from 'characters' for clarity
    private final List<String> characterNames;
    private int currentIndex = 0;

    public CharacterManager(List<String> imageFilePaths, List<String> names) { // Parameter name changed
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

    public void nextCharacter() {
        if (imageFiles.isEmpty()) return; // Should not happen if constructor validation works
        currentIndex = (currentIndex + 1) % imageFiles.size();
    }

    public void previousCharacter() { // Added for completeness, might be useful later
        if (imageFiles.isEmpty()) return;
        currentIndex = (currentIndex - 1 + imageFiles.size()) % imageFiles.size();
    }

    // Renamed from getImage() to be more specific, matching what MainMenuController was expecting
    public String getCurrentImagePath() {
        if (imageFiles.isEmpty()) return null;
        return imageFiles.get(currentIndex);
    }

    public String getCurrentName() {
        if (characterNames.isEmpty()) return null; // Consistency check
        return characterNames.get(currentIndex);
    }

    public int getCurrentIndex() { // Keep one method for getting the index
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        if (index >= 0 && index < imageFiles.size()) {
            this.currentIndex = index;
        } else {
            System.err.println("CharacterManager: Attempt to set invalid current index: " + index);
            // Optionally throw an IllegalArgumentException here as well for stricter error handling
        }
    }

    
    public int getCharacterCount() {
        return imageFiles.size();
    }
    
}