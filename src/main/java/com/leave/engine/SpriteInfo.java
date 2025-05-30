package com.leave.engine;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpriteInfo {
    private String path; 
    private int frameWidth;
    private int frameHeight;

    @JsonProperty("cols") 
    private int numCols;
    private int totalFrames;
    private double fps;
    private boolean loop;

    
    public SpriteInfo() {}

    
    public String getPath() { return path; }
    public int getFrameWidth() { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
    public int getNumCols() { return numCols; }
    public int getTotalFrames() { return totalFrames; }
    public double getFps() { return fps; }
    public boolean isLoop() { return loop; } 

    
    public void setPath(String path) { this.path = path; }
    public void setFrameWidth(int frameWidth) { this.frameWidth = frameWidth; }
    public void setFrameHeight(int frameHeight) { this.frameHeight = frameHeight; }
    public void setNumCols(int numCols) { this.numCols = numCols; }
    public void setTotalFrames(int totalFrames) { this.totalFrames = totalFrames; }
    public void setFps(double fps) { this.fps = fps; }
    public void setLoop(boolean loop) { this.loop = loop; }
}
