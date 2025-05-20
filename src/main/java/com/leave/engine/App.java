package com.leave.engine;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font; 
import javafx.stage.Stage;

public class App extends Application {

    private static Scene primaryScene;
    public static Font HORROR_FONT; // Made static as per your previous code
    public static String HORROR_FONT_FAMILY_NAME; // Made static as per your previous code
    private static Stage appPrimaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        appPrimaryStage = stage;
        appPrimaryStage.setTitle("Leave!");
        appPrimaryStage.setFullScreen(true);
        appPrimaryStage.setFullScreenExitHint("");

        System.out.println("Application starting...");

        loadCustomFonts();
        loadInitialView(); 

        appPrimaryStage.show();
        System.out.println("Primary stage shown.");
    }

    private void loadInitialView() {
        try {
            URL fxmlUrl = App.class.getResource("/com/leave/engine/gameEntry.fxml");
            if (fxmlUrl == null) {
                System.err.println("FATAL ERROR: gameEntry.fxml not found!");
                // Consider a more user-friendly error popup or graceful exit
                Platform.exit(); // Or throw new RuntimeException to stop the app
                return;
            }
            System.out.println("Loading initial view FXML from: " + fxmlUrl.toExternalForm());

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setClassLoader(App.class.getClassLoader()); // Explicitly set the classloader
            Parent rootNode = loader.load();
            MainMenuController controller = loader.getController();
            

            if (primaryScene == null) {
                primaryScene = new Scene(rootNode);
                URL cssUrl = App.class.getResource("/com/leave/engine/style.css");
                if (cssUrl != null) {
                    primaryScene.getStylesheets().add(cssUrl.toExternalForm());
                    System.out.println("Global style.css loaded.");
                } else {
                    System.err.println("WARNING: style.css not found at /com/leave/engine/style.css");
                }
            } else {
                primaryScene.setRoot(rootNode);
            }

            appPrimaryStage.setScene(primaryScene);

            if (controller != null) {
                // Call setupGlobalKeyListener after the scene is set and possibly shown,
                // as it might try to access the scene to attach listeners.
                Platform.runLater(controller::setupGlobalKeyListener);
            }

        } catch (IOException e) {
            System.err.println("ERROR: Failed to load initial view FXML: " + e.getMessage());
            e.printStackTrace();
            // Consider a more user-friendly error popup
            // You might want to Platform.exit() here too if this is a fatal error.
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("UNEXPECTED ERROR during initial view loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCustomFonts() {
        System.out.println("Loading custom fonts...");
        try {
            // Assuming Le-Mano.ttf is in src/main/resources/com/leave/engine/data/
            String fontPath = "/com/leave/engine/data/Le-Mano.ttf";
            URL fontUrl = App.class.getResource(fontPath);
            if (fontUrl != null) {
                HORROR_FONT = Font.loadFont(fontUrl.toExternalForm(), 20); // Load with a default size
                if (HORROR_FONT != null) {
                    HORROR_FONT_FAMILY_NAME = HORROR_FONT.getFamily();
                    System.out.println("Custom font '" + HORROR_FONT_FAMILY_NAME + "' loaded successfully from " + fontPath);
                } else {
                    System.err.println("Failed to load custom font from path: " + fontPath + ". Font.loadFont returned null.");
                }
            } else {
                System.err.println("Custom font file not found at resource path: " + fontPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            e.printStackTrace();
        }
        // Fallback or ensure default font is used if HORROR_FONT is null
        if (HORROR_FONT == null) {
            System.err.println("Using system default font as custom font loading failed.");
            HORROR_FONT = Font.getDefault(); // Or a specific named system font
            HORROR_FONT_FAMILY_NAME = HORROR_FONT.getFamily();
        }
    }

    public static void setRoot(String fxml) throws IOException {
        if (appPrimaryStage == null || primaryScene == null) {
            System.err.println("ERROR: Main stage or scene not initialized for setRoot.");
            return; // Or throw an exception
        }
        System.out.println("Attempting to set root to: " + fxml + ".fxml");
        // Construct the full resource path
        String fullFxmlPath = "/com/leave/engine/" + fxml + ".fxml";
        Parent rootNode = loadFXMLByFullPath(fullFxmlPath); // Use helper that takes full path
        if (rootNode != null) {
            primaryScene.setRoot(rootNode);
            System.out.println("Primary scene root changed to: " + fullFxmlPath);
        } else {
            System.err.println("Error: Could not set root. FXML for '" + fullFxmlPath + "' not loaded.");
        }
    }

    // Helper method to load FXML given a full resource path
    private static Parent loadFXMLByFullPath(String fullFxmlResourcePath) throws IOException {
        URL fxmlUrl = App.class.getResource(fullFxmlResourcePath);
        if (fxmlUrl == null) {
            throw new IOException("FATAL ERROR: FXML file not found at resource path: " + fullFxmlResourcePath);
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        // If controllers for these FXMLs also need resource loading for images, etc.,
        // you might want to set their classloader too, though usually FXMLLoader handles it
        // based on the classloader used to load the FXML itself if not explicitly set.
        fxmlLoader.setClassLoader(App.class.getClassLoader()); // Good practice here as well
        return fxmlLoader.load();
    }


    public static void main(String[] args) {
        launch(args);
    }
}