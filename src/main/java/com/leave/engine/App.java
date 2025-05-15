package com.leave.engine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font; // Import Font
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream; // Import InputStream
import java.net.URL;

public class App extends Application {

    private static Scene scene;
    public static Font HORROR_FONT; // Make it accessible if needed programmatically
    public static String HORROR_FONT_FAMILY_NAME;

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Application starting...");

        // Load custom font(s)
        loadCustomFonts(); // Call the new method

        Parent rootNode = loadFXML("title"); // Or "mainMenu" if you skip title
        scene = new Scene(rootNode, 800, 600); // Increased size slightly for example

        URL cssUrl = getClass().getResource("style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("SUCCESS: Global style.css loaded from: " + cssUrl.toExternalForm());
        } else {
            System.err.println("WARNING: Could not find global style.css in package com/leave/engine/");
        }

        stage.setTitle("Leave! - A Tale of Shadows"); // Horror-themed title
        stage.setScene(scene);
        stage.show();
        System.out.println("Application stage is now showing.");
    }

    private void loadCustomFonts() {
        String fontPath = "/com/leave/engine/data/Le-Mano.ttf"; // ADJUST FILENAME
        try (InputStream fontStream = App.class.getResourceAsStream(fontPath)) {
            if (fontStream == null) {
                System.err.println("FATAL ERROR: Horror font not found at: " + fontPath);
                return;
            }
            HORROR_FONT = Font.loadFont(fontStream, 24); // Load with a default size
            if (HORROR_FONT != null) {
                HORROR_FONT_FAMILY_NAME = HORROR_FONT.getFamily();
                System.out.println("SUCCESS: Loaded custom font: " + HORROR_FONT_FAMILY_NAME);
            } else {
                System.err.println("ERROR: Failed to load custom font from path: " + fontPath);
            }
        } catch (IOException e) {
            System.err.println("ERROR: IOException while trying to load custom font: " + fontPath);
            e.printStackTrace();
        }
    }

    // ... rest of App.java (setRoot, loadFXML, main) ...
    public static void setRoot(String fxml) throws IOException {
        Parent rootNode = loadFXML(fxml);
        if (rootNode != null) {
            scene.setRoot(rootNode);
            System.out.println("Scene root changed to: " + fxml + ".fxml");
        } else {
            System.err.println("Error: Could not set root. FXML loading returned null for: " + fxml);
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String fxmlFileName = fxml + ".fxml";
        URL fxmlUrl = App.class.getResource(fxmlFileName);

        if (fxmlUrl == null) {
            System.err.println("FATAL ERROR: FXML file not found: " + fxmlFileName + " in package com.leave.engine");
            throw new IOException("Cannot find FXML file: " + fxmlFileName + " in " + App.class.getPackage().getName());
        }

        System.out.println("Attempting to load FXML: " + fxmlUrl.toExternalForm());
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            System.err.println("FATAL ERROR: Failed to load FXML file: " + fxmlUrl.toExternalForm());
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}