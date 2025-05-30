// In package com.leave.engine.storydata
package com.leave.engine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InteractiveObjectInfo {
    private String id;
    private String name; // Display name
    @JsonProperty("imagePath") // Ensure mapping if JSON key differs slightly
    private String imagePath;
    private int x; // Position
    private int y; // Position
    private String requiredItem; // Optional: item ID needed to interact
    private String actionOnInteract; // Optional: action to perform (e.g., "TAKE_SHOVEL")
    private String yieldsItem; // Optional: item ID this object gives
    private boolean consumedOnInteract; // Optional: if the object disappears after interaction

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    // ... getters and setters for new fields ...

    public String getRequiredItem() {
        return requiredItem;
    }

    public void setRequiredItem(String requiredItem) {
        this.requiredItem = requiredItem;
    }

    public String getActionOnInteract() {
        return actionOnInteract;
    }

    public void setActionOnInteract(String actionOnInteract) {
        this.actionOnInteract = actionOnInteract;
    }

    public String getYieldsItem() {
        return yieldsItem;
    }

    public void setYieldsItem(String yieldsItem) {
        this.yieldsItem = yieldsItem;
    }

    public boolean isConsumedOnInteract() {
        return consumedOnInteract;
    }

    public void setConsumedOnInteract(boolean consumedOnInteract) {
        this.consumedOnInteract = consumedOnInteract;
    }
}