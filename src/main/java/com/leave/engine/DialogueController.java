package com.leave.engine;

import java.io.IOException;

import javafx.fxml.FXML;

public class DialogueController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
