package com.leave.engine;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
;

public class StoryLoader {
    public GameStory loadStory(String resourcePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = StoryLoader.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("Cannot find story resource: " + resourcePath);
        }
        return mapper.readValue(inputStream, GameStory.class);
    }
}