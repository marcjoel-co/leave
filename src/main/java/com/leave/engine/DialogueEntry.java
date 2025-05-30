package com.leave.engine;

public class DialogueEntry {
    private String speaker;
    private String line;
    private String style; 
    private String portraitPath;
    
    public String getSpeaker()
    { 
        return speaker; 
    }
    public void setSpeaker(String speaker) 
    { 
        this.speaker = speaker; 
    }
    public String getLine() 
    {
         return line;
    }
    public void setLine(String line) 
    { 
        this.line = line; 
    }
    public String getStyle() 
    { 
        return style; 
    }
    public void setStyle(String style) 
    { 
        this.style = style; 
    }

    public String getPortraitPath() {
        return portraitPath;
    }

    public void setPortraitPath(String portraitPath) {
        this.portraitPath = portraitPath;
    }
}