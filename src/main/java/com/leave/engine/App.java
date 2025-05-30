package com.leave.engine;

import java.io.IOException;
import java.net.URL;

import com.leave.engine.utils.AudioManager; 

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font; 
import javafx.stage.Stage; 

public class App extends Application { // WOah polymorphism

    private static Scene primaryScene;
    public static Font HORROR_FONT; 
    public static String HORROR_FONT_FAMILY_NAME; 
    private static Stage appPrimaryStage;
    private static GameManager gameManager;
    private AudioManager audioManager;

    private static final String STORY_JSON_PATH = "/com/leave/engine/data/sao.json"; // our Json file
    private static final String DEFAULT_FONT_PATH = "/com/leave/engine/data/Le-Mano.ttf"; // horror font


    // prep to load
    @Override
    public void init() throws Exception {
        super.init();
        audioManager = AudioManager.getInstance();
        audioManager.loadTextBlipSound("/com/leave/engine/data/audio/blip.wav");
    }

    // stops the app
    @Override
    public void stop() throws Exception {
        super.stop();
        if (audioManager != null) {
            audioManager.shutdown();
        }
        System.out.println("Application stopped.");
    }

    @Override
    public void start(Stage stage) {
        // sets up the window similar to a JFrame but javafx
        appPrimaryStage = stage;
        
        appPrimaryStage.setTitle("Delulu"); 
        appPrimaryStage.setFullScreen(true); 
        appPrimaryStage.setFullScreenExitHint("");
        appPrimaryStage.setResizable(false);  

        /* most of the println are there for debugging */
        System.out.println("App.start(): JavaFX Application Thread INITIALIZING.");

        
        boolean canProceed = true;
        gameManager = GameManager.getInstance(); // obtains a gamemanager instance
        System.out.println("App.start(): GameManager instance obtained.");

        // Tries to load story 
        try {
            System.out.println("App.start(): Attempting to load story from: " + STORY_JSON_PATH);
            gameManager.loadStory(STORY_JSON_PATH);
            System.out.println("App.start(): Story loaded. Game Title: " + gameManager.getGameTitle());
        } catch (IOException e) {
            System.err.println("App.start(): CRITICAL ERROR - Could not load story.");
            e.printStackTrace();
            canProceed = false; // Set to false if story loading fails
            // Optionally, show an alert dialog here for the user before exiting or trying to proceed
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fatal Error");
                alert.setHeaderText("Game Data Load Failure");
                alert.setContentText("The game story could not be loaded. Please ensure the game files are intact. See console for details.");
                alert.showAndWait();
                Platform.exit(); // Exit application if critical data is missing
            });
            return; // Exit start method
        }

        if (canProceed) { // This check is now crucial as we might have returned early
            System.out.println("App.start(): Calling gameManager.startGame()...");
            gameManager.startGame();
            System.out.println("App.start(): gameManager.startGame() completed. Player name is currently: '" + gameManager.getCurrentPlayerName() + "'");
        }

        loadCustomFonts();
        System.out.println("App.start(): Loading initial FXML view (gameEntry.fxml)...");
        loadInitialViewAndSetScene("gameEntry"); 

        
        if (appPrimaryStage.getScene() != null) { 
             appPrimaryStage.show();
             System.out.println("App.start(): Stage should now be visible.");
        } else {
            System.err.println("App.start(): ERROR! No scene was set on the stage. Cannot show.");
        }
    }

    // ... (App.setRoot and loadInitialViewAndSetScene methods as before, but with slight modifications below) ...

    /**
     * Loads the specified FXML, sets it as the root of the primary scene.
     * Also handles controller callbacks for global key listener setup if MainMenuController.
     * Creates a fallback error scene if FXML loading fails.
     * @param fxmlName The base name of the FXML file (e.g., "gameEntry")
     */
    private static void loadInitialViewAndSetScene(String fxmlName) {
        Parent rootNode = null;
        Object controllerObj = null; // We need this to call setupGlobalKeyListener
        String fullFxmlPath = "/com/leave/engine/" + fxmlName + ".fxml";

        try {
            URL fxmlUrl = App.class.getResource(fullFxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML resource: " + fullFxmlPath);
            }
            System.out.println("App.loadInitialView(): Loading FXML from: " + fxmlUrl.toExternalForm());

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setClassLoader(App.class.getClassLoader()); // Good practice for modularity
            rootNode = loader.load(); // Can throw IOException
            controllerObj = loader.getController(); // Get the controller instance here
            System.out.println("App.loadInitialView(): " + fxmlName + ".fxml loaded successfully.");

        } catch (Exception e) {
            System.err.println("App.loadInitialView(): ERROR loading or processing FXML '" + fullFxmlPath + "': " + e.getMessage());
            e.printStackTrace();
            rootNode = null; // Ensure rootNode is null on error
        }

        if (rootNode != null) {
            if (primaryScene == null) {
                primaryScene = new Scene(rootNode);
                
                
                URL cssUrl = App.class.getResource("/com/leave/engine/style.css");
                if (cssUrl != null) {
                    primaryScene.getStylesheets().add(cssUrl.toExternalForm());
                    System.out.println("App.loadInitialView(): style.css loaded.");
                } else { System.err.println("App.loadInitialView(): WARNING - style.css not found at /com/leave/engine/style.css."); }
            } else {
                primaryScene.setRoot(rootNode); // Update existing scene's content
            }
            // IMPORTANT: Set up global key listener *after* the scene is set on the stage (or at least scene.getRoot() is available)
            // And pass the controller if needed
            if (controllerObj instanceof MainMenuController) {
                System.out.println("App.loadInitialView(): Queued setupGlobalKeyListener for MainMenuController.");
                final MainMenuController mainMenuController = (MainMenuController) controllerObj;
                Platform.runLater(() -> { // Ensure this runs after the scene is fully rendered/attached
                    mainMenuController.setupGlobalKeyListener();
                });
            }

        } else {
            System.err.println("App.loadInitialView(): FXML rootNode is null. Creating fallback error scene.");
            Label errorLabel = new Label(
                "Critical Error: Could not load main game interface ('" + fxmlName + ".fxml').\n" +
                "Please check the console log for details."
            );
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-padding: 20px; -fx-wrap-text: true; -fx-alignment: center;");
            StackPane errorPane = new StackPane(errorLabel);
            errorPane.setStyle("-fx-background-color: #1c1c1c;");
            
            double width = appPrimaryStage.getWidth() > 100 ? appPrimaryStage.getWidth() : 1280;
            double height = appPrimaryStage.getHeight() > 100 ? appPrimaryStage.getHeight() : 720;
            primaryScene = new Scene(errorPane, width, height); // Ensure scene has dimensions
        }
        
        appPrimaryStage.setScene(primaryScene);
        System.out.println("App.loadInitialView(): Primary scene has been set on the stage.");
    }

    public static void setRoot(String fxml) throws IOException {
        // ... (This method would likely call loadInitialViewAndSetScene internally for robustness) ...
        // This is what you would call from MainMenuController to switch to 'gameplay.fxml'
        loadInitialViewAndSetScene(fxml); // Assuming this is how your App.setRoot works
    }

    public static void setRoot(String fxml, java.util.function.Consumer<Object> controllerCallback) throws IOException {
        Parent rootNode = null;
        Object controllerObj = null;
        String fullFxmlPath = "/com/leave/engine/" + fxml + ".fxml";

        try {
            URL fxmlUrl = App.class.getResource(fullFxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML resource: " + fullFxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setClassLoader(App.class.getClassLoader());
            rootNode = loader.load();
            controllerObj = loader.getController();
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + fxml);
            e.printStackTrace();
            throw new IOException("Failed to load FXML: " + fxml, e);
        }

        if (primaryScene == null) {
            primaryScene = new Scene(rootNode);
        } else {
            primaryScene.setRoot(rootNode);
        }
        // Ensure stage is updated if this setRoot is called before stage.show()
        if (appPrimaryStage.getScene() == null) {
            appPrimaryStage.setScene(primaryScene);
        }

        if (controllerCallback != null && controllerObj != null) {
            final Object finalControllerObj = controllerObj;
            Platform.runLater(() -> controllerCallback.accept(finalControllerObj));
        }
    }


    private void loadCustomFonts() {
        System.out.println("App.loadCustomFonts(): Loading...");
        try {
            // Load font from classpath
            URL fontUrl = getClass().getResource(DEFAULT_FONT_PATH);
            if (fontUrl != null) {
                HORROR_FONT = Font.loadFont(fontUrl.toExternalForm(), 20); // Load with a base size
                if (HORROR_FONT != null) {
                    // HORROR_FONT_FAMILY_NAME = HORROR_FONT.getFamily();
                    // System.out.println("App.loadCustomFonts(): Font '" + HORROR_FONT_FAMILY_NAME + "' loaded from " + DEFAULT_FONT_PATH);
                } else {
                    System.err.println("App.loadCustomFonts(): WARNING - Font.loadFont returned null for " + DEFAULT_FONT_PATH);
                }
            } else {
                System.err.println("App.loadCustomFonts(): WARNING - Font resource not found at " + DEFAULT_FONT_PATH);
            }
        } catch (Exception e) {
            System.err.println("App.loadCustomFonts(): ERROR loading font: " + DEFAULT_FONT_PATH);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("App.main(): Launching JavaFX application via Application.launch()...");
        launch(); // This calls the start method indirectly
    }
}