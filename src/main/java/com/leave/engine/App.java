package com.leave.engine; // Ensure this matches your package structure

import java.io.IOException;
import java.io.InputStream; // Crucial for UI updates from non-FX threads
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader; // Optional, if you want to explicitly set scene background
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class App extends Application {

    // Store scenes if you plan to switch between them frequently,
    // or just create them on demand.
    // For a simple logo -> main menu, creating on demand is fine too.
    private static Scene logoScene;
    private static Scene mainScene; // Static if setRoot is to be static

    public static Font HORROR_FONT;
    public static String HORROR_FONT_FAMILY_NAME;

    private static Stage primaryStage;
    // No separate logoStage needed with this approach

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Leave! - Loading..."); // Initial title
        this.primaryStage.setFullScreen(true); // Set to full screen ONCE at the very beginning

        System.out.println("Application starting... Primary stage set to full screen.");

        loadCustomFonts();
        showLogoAnimationOnPrimaryStage(); // Show logo on the primary stage
    }

    private void showLogoAnimationOnPrimaryStage() {
        try {
            // Ensure this path is correct relative to your resources folder
            // e.g., src/main/resources/com/leave/engine/title.fxml
            URL logoFxmlUrl = App.class.getResource("/com/leave/engine/title.fxml");
            if (logoFxmlUrl == null) {
                System.err.println("ERROR: title.fxml not found at /com/leave/engine/title.fxml");
                // Fallback directly to main menu if logo FXML is missing
                Platform.runLater(this::safeLoadAndSetMainMenuScene);
                return;
            }
            System.out.println("Loading logo FXML from: " + logoFxmlUrl.toExternalForm());

            FXMLLoader loader = new FXMLLoader(logoFxmlUrl);
            Parent logoRoot = loader.load();
            TitlePanel logoController = loader.getController();

            logoController.setOnAnimationFinished(() -> {
                System.out.println("Logo animation finished. Switching to main menu scene.");
                // Use Platform.runLater for UI changes from non-UI threads or after events
                Platform.runLater(this::safeLoadAndSetMainMenuScene);
            });

            if (logoScene == null) { // Create logo scene once if needed
                 logoScene = new Scene(logoRoot);
                 // Example: Explicitly black background for the logo scene if FXML doesn't handle it.
                 // Adjust if your FXML root (e.g., StackPane in title.fxml) already has a black background.
                 // logoScene.setFill(Color.BLACK);
            } else {
                logoScene.setRoot(logoRoot); // Update if already created
            }


            primaryStage.setScene(logoScene);
            if (!primaryStage.isShowing()) {
                // This show() will happen on the first run, making the full-screen stage visible with the logo
                System.out.println("Showing primary stage (with logo content).");
                primaryStage.show();
            } else {
                System.out.println("Primary stage already showing, set to logo scene.");
            }

            System.out.println("Playing logo animation.");
            logoController.playAnimation();

        } catch (IOException e) {
            System.err.println("ERROR: Failed to load or show logo animation: " + e.getMessage());
            e.printStackTrace();
            // Fallback: if logo animation fails, try to load main menu directly
            Platform.runLater(this::safeLoadAndSetMainMenuScene);
        }
    }

    // Wrapper method for exception handling for main menu scene loading
    private void safeLoadAndSetMainMenuScene() {
        try {
            loadAndSetMainMenuScene();
        } catch (IOException e) {
            System.err.println("FATAL ERROR: Could not load and set main menu scene: " + e.getMessage());
            e.printStackTrace();
            // Consider showing an error dialog and exiting:
            // Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load main application content.", ButtonType.OK);
            // alert.showAndWait();
            // Platform.exit();
        }
    }

    // Method to load and set the main menu scene on the primaryStage
    private void loadAndSetMainMenuScene() throws IOException {
        primaryStage.setTitle("Leave! - Main Menu"); // Update title
        System.out.println("Loading main menu FXML to set as primary stage scene.");

        // Ensure this path is correct
        Parent mainMenuRootNode = loadFXML("/com/leave/engine/mainMenu");
        if (mainMenuRootNode == null) {
            System.err.println("FATAL ERROR: Main menu FXML ('mainMenu.fxml') could not be loaded. Application cannot proceed.");
            return;
        }

        if (mainScene == null) { // Create main scene once if it doesn't exist
            mainScene = new Scene(mainMenuRootNode); // Size will be full screen

            // Load global CSS for the main scene
            // Ensure this path is correct
            URL cssUrl = App.class.getResource("/com/leave/engine/style.css");
            if (cssUrl != null) {
                mainScene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("SUCCESS: Global style.css loaded for main scene from: " + cssUrl.toExternalForm());
            } else {
                System.err.println("WARNING: Could not find global style.css at /com/leave/engine/style.css");
            }
        } else { // If main scene already exists, just update its root content
            mainScene.setRoot(mainMenuRootNode);
            System.out.println("Main scene root updated with new mainMenu content.");
        }

        primaryStage.setScene(mainScene);
        // primaryStage.setFullScreen(true); // NO - Already set at App.start()
        // primaryStage.show(); // NO - Already shown with logo, just scene is changing

        System.out.println("Primary stage is now displaying the main menu scene.");
        // If primary stage wasn't shown for some reason (e.g. logo failed before show)
        if (!primaryStage.isShowing()) {
             System.out.println("Primary stage was not visible, showing now with main menu.");
             primaryStage.show();
        }
    }


    private void loadCustomFonts() {
        // Ensure this path is correct
        String fontPath = "/com/leave/engine/data/Le-Mano.ttf";
        System.out.println("Attempting to load font from: " + fontPath);
        try (InputStream fontStream = App.class.getResourceAsStream(fontPath)) {
            if (fontStream == null) {
                System.err.println("FATAL ERROR: Horror font not found at: " + fontPath + ". Check path and ensure file is in resources.");
                return;
            }
            HORROR_FONT = Font.loadFont(fontStream, 24); // Default size, can be overridden by CSS
            if (HORROR_FONT != null) {
                HORROR_FONT_FAMILY_NAME = HORROR_FONT.getFamily();
                System.out.println("SUCCESS: Loaded custom font. Family: '" + HORROR_FONT_FAMILY_NAME +
                                   "', Name: '" + HORROR_FONT.getName() + "', Style: '" + HORROR_FONT.getStyle() + "'");
            } else {
                System.err.println("ERROR: Font.loadFont returned null. Font at path: " + fontPath + " might be invalid or unsupported.");
            }
        } catch (Exception e) { // Catch generic Exception to see any other loading issues
            System.err.println("ERROR: Exception while trying to load custom font: " + fontPath);
            e.printStackTrace();
        }
    }

    // setRoot should operate on mainScene which is displayed on primaryStage
    public static void setRoot(String fxml) throws IOException {
        if (primaryStage == null || mainScene == null) {
            System.err.println("ERROR: Main stage or main scene is not initialized. Cannot set root.");
            return;
        }
        System.out.println("Attempting to set root for main scene to: " + fxml + ".fxml");
        // Ensure path is like "/com/leave/engine/yourSubFxml"
        Parent rootNode = loadFXML("/com/leave/engine/" + fxml);
        if (rootNode != null) {
            mainScene.setRoot(rootNode);
            // The primaryStage is already showing mainScene, so changing mainScene's root updates the view.
            System.out.println("Main scene root successfully changed to: " + fxml + ".fxml");
        } else {
            System.err.println("Error: Could not set root. FXML loading returned null for: " + fxml);
        }
    }

    // Helper to load FXML, expects an absolute path from classpath root
    private static Parent loadFXML(String fxmlResourcePathWithoutExtension) throws IOException {
        String fxmlFileNameWithExtension = fxmlResourcePathWithoutExtension + ".fxml";
        URL fxmlUrl = App.class.getResource(fxmlFileNameWithExtension);

        if (fxmlUrl == null) {
            String errorMessage = "FATAL ERROR: FXML file not found at resource path: " + fxmlFileNameWithExtension;
            System.err.println(errorMessage);
            throw new IOException(errorMessage);
        }

        System.out.println("Attempting to load FXML: " + fxmlUrl.toExternalForm());
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            System.err.println("FATAL ERROR: Failed to load FXML file: " + fxmlUrl.toExternalForm());
            e.printStackTrace();
            throw e; // Re-throw to indicate failure to the caller
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}