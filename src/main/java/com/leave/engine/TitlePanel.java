package com.leave.engine;

import java.io.IOException;

import javafx.fxml.FXML;

public class TitlePanel {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("mainMenu");
    }
}
