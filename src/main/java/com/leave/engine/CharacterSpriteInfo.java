package com.leave.engine; 

import com.fasterxml.jackson.annotation.JsonProperty;

public class CharacterSpriteInfo {
    private String id;
    private String path;
    private int frameWidth;
    private int frameHeight;
    @JsonProperty("cols") // Using @JsonProperty to map "cols" in JSON to numCols in Java
    private int numCols;
    private int totalFrames;
    private double fps;
    private boolean loop;
    private boolean visibleInitially;

    // Fields for positioning - matching the JSON "positionX" and assuming "positionY"
    private double positionX; 
    private double positionY; 

    // Default constructor - ESSENTIAL for Jackson
    public CharacterSpriteInfo() {}

    // Getters
    public String getId() { return id; }
    public String getPath() { return path; }
    public int getFrameWidth() { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
    public int getNumCols() { return numCols; }
    public int getTotalFrames() { return totalFrames; }
    public double getFps() { return fps; }
    public boolean isLoop() { return loop; }

    public double getPositionX() { return positionX; } 
    public double getPositionY() { return positionY; } 

    
    public void setId(String id) { this.id = id; }
    public void setPath(String path) { this.path = path; }
    public void setFrameWidth(int frameWidth) { this.frameWidth = frameWidth; }
    public void setFrameHeight(int frameHeight) { this.frameHeight = frameHeight; }
    public void setNumCols(int numCols) { this.numCols = numCols; }
    public void setTotalFrames(int totalFrames) { this.totalFrames = totalFrames; }
    public void setFps(double fps) { this.fps = fps; }
    public void setLoop(boolean loop) { this.loop = loop; }
    public void setPositionX(double positionX) { this.positionX = positionX; } 
    public void setPositionY(double positionY) { this.positionY = positionY; } 
     public boolean isVisibleInitially() {
        return visibleInitially;
    }

    public void setVisibleInitially(boolean visibleInitially) {
        this.visibleInitially = visibleInitially;
    }
}