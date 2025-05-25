package com.leave.engine;

import java.io.IOException;
import java.net.URL;

import com.leave.engine.utils.AudioManager; // Keep for promptForPlayerName (if you ever re-add it or for other dialogs)

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane; // Explicitly for error label
import javafx.scene.text.Font; // Keep if you might use it later, not for current flow
import javafx.stage.Stage; // Explicitly for error pane

public class App extends Application {

    private static Scene primaryScene;
    public static Font HORROR_FONT;
    public static String HORROR_FONT_FAMILY_NAME;
    private static Stage appPrimaryStage;
    private static GameManager gameManager;
    private AudioManager audioManager;

    private static final String STORY_JSON_PATH = "/com/leave/engine/data/sao.json";
    private static final String DEFAULT_FONT_PATH = "/com/leave/engine/data/Le-Mano.ttf"; // Define font path

    @Override
public void init() throws Exception { // init() is called before start()
    super.init();
    audioManager = AudioManager.getInstance();
    // Preload common sounds if you have them
    audioManager.preloadSoundEffects(
        "/com/leave/engine/audio/clicker.wav",
        "/com/leave/engine/audio/heartcl.wav"
    );
    audioManager.loadTextBlipSound("/com/leave/engine/audio/blip.wav"); // Load your text blip
}


@Override
public void stop() throws Exception { // Called when JavaFX app exits
    super.stop();
    if (audioManager != null) {
        audioManager.shutdown();
    }
    System.out.println("Application stopped.");
}
    @Override
    public void start(Stage stage) {
        appPrimaryStage = stage;
        System.out.println("----------------------------------------------------");
        System.out.println("App.start(): JavaFX Application Thread INITIALIZING.");
        System.out.println("----------------------------------------------------");

        appPrimaryStage.setTitle("Leave!");
        appPrimaryStage.setFullScreen(true);
        appPrimaryStage.setFullScreenExitHint("");

        boolean canProceedToUI = true;

        // 1. Initialize GameManager Singleton Instance
        gameManager = GameManager.getInstance();
        System.out.println("App.start(): GameManager instance obtained.");

        // 2. Load Story Data
        try {
            System.out.println("App.start(): Attempting to load story from: " + STORY_JSON_PATH);
            gameManager.loadStory(STORY_JSON_PATH);
            System.out.println("App.start(): Story loaded. Game Title: " + gameManager.getGameTitle());
        } catch (IOException e) {
            // This is critical, show an error and don't proceed with game logic dependent on story
            System.err.println("App.start(): CRITICAL ERROR - Could not load story. Cannot start game logic.");
            e.printStackTrace();
            canProceedToUI = false; // Game logic cannot start
            // We will still try to show a UI, even if it's an error message.
        }

        // 3. Start Game Logic in GameManager (only if story loaded)
        if (canProceedToUI) { // Check if story loaded
            System.out.println("App.start(): Calling gameManager.startGame() with default player name...");
            gameManager.startGame(); // Player name will be set by MainMenuController later
            System.out.println("App.start(): gameManager.startGame() completed.");
        } else {
             System.out.println("App.start(): Skipping gameManager.startGame() due to previous errors.");
        }

        // 4. Load Custom Fonts (this should generally always be attempted)
        loadCustomFonts();

        // 5. Attempt to Load Initial FXML View
        System.out.println("App.start(): Loading initial FXML view (gameEntry.fxml)...");
        loadInitialViewAndSetScene("gameEntry"); // Renamed for clarity

        // 6. Show the Stage
        audioManager.playBackgroundMusic("/com/leave/engine/audio/mainn.wav", true, 0.7);
        // This will happen even if loadInitialViewAndSetScene had to set a fallback error scene
        if (appPrimaryStage.getScene() != null) {
            appPrimaryStage.show();
            System.out.println("App.start(): Primary stage shown.");
        } else {
            // This case should ideally be prevented by loadInitialViewAndSetScene always setting *some* scene
            System.err.println("App.start(): CRITICAL - Primary scene is STILL null after attempts to load views. Stage cannot be shown.");
            // Show a final, very basic JavaFX alert if possible, then exit.
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Catastrophic failure setting up initial UI. Application will exit.");
                alert.setTitle("Fatal Application Error");
                alert.showAndWait();
                Platform.exit();
            });
            return;
        }

        // 7. If setup had issues earlier, show a more informative alert *now* that a window is visible
        if (!canProceedToUI) { // This flag tracks if story loading or critical FXML loading failed
             showPostLaunchErrorAlert("Application Initialization Incomplete",
                                   "Could not fully initialize the game (e.g., story data failed to load). Some features may be unavailable or the game may not start correctly. Please check console logs.");
        }

        System.out.println("----------------------------------------------------");
        System.out.println("App.start(): COMPLETED.");
        System.out.println("----------------------------------------------------");
    }


    /**
     * Loads the specified FXML, sets it as the root of the primary scene.
     * Also handles controller callbacks for global key listener setup if MainMenuController.
     * Creates a fallback error scene if FXML loading fails.
     * @param fxmlName The base name of the FXML file (e.g., "gameEntry")
     */
    private void loadInitialViewAndSetScene(String fxmlName) {
        Parent rootNode = null;
        Object controllerObj = null;
        String fullFxmlPath = "/com/leave/engine/" + fxmlName + ".fxml";

        try {
            URL fxmlUrl = App.class.getResource(fullFxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML resource: " + fullFxmlPath);
            }
            System.out.println("App.loadInitialView(): Loading FXML from: " + fxmlUrl.toExternalForm());

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setClassLoader(App.class.getClassLoader());
            rootNode = loader.load(); // Can throw IOException
            controllerObj = loader.getController();
            System.out.println("App.loadInitialView(): " + fxmlName + ".fxml loaded successfully.");

        } catch (Exception e) { // Catch IOException from load() or any other during FXML processing
            System.err.println("App.loadInitialView(): ERROR loading or processing FXML '" + fullFxmlPath + "': " + e.getMessage());
            e.printStackTrace();
            // rootNode will remain null
        }

        // Set up the scene, using fallback if rootNode is null
        if (rootNode != null) {
            if (primaryScene == null) {
                primaryScene = new Scene(rootNode);
                URL cssUrl = App.class.getResource("/com/leave/engine/style.css");
                if (cssUrl != null) {
                    primaryScene.getStylesheets().add(cssUrl.toExternalForm());
                } else { System.err.println("App.loadInitialView(): WARNING - style.css not found."); }
            } else {
                primaryScene.setRoot(rootNode);
            }
        } else {
            // FXML loading failed, create and set a fallback basic error scene
            System.err.println("App.loadInitialView(): FXML rootNode is null. Creating fallback error scene.");
            Label errorLabel = new Label(
                "Critical Error: Could not load main game interface ('" + fxmlName + ".fxml').\n" +
                "Please check the console log for details."
            );
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-padding: 20px; -fx-wrap-text: true; -fx-alignment: center;");
            StackPane errorPane = new StackPane(errorLabel);
            errorPane.setStyle("-fx-background-color: #1c1c1c;");
            // Use stage dimensions if available and sensible, otherwise default
            double width = (appPrimaryStage != null && appPrimaryStage.getWidth() > 100) ? appPrimaryStage.getWidth() : 1280;
            double height = (appPrimaryStage != null && appPrimaryStage.getHeight() > 100) ? appPrimaryStage.getHeight() : 720;
            primaryScene = new Scene(errorPane, width, height);
        }
        
        appPrimaryStage.setScene(primaryScene);
        System.out.println("App.loadInitialView(): Primary scene has been set on the stage.");

        // Call setupGlobalKeyListener AFTER scene is set
        if (controllerObj instanceof MainMenuController) {
            MainMenuController mainMenuController = (MainMenuController) controllerObj;
            Platform.runLater(mainMenuController::setupGlobalKeyListener);
            System.out.println("App.loadInitialView(): Queued setupGlobalKeyListener for MainMenuController.");
        }
    }

    private void loadCustomFonts() {
        System.out.println("App.loadCustomFonts(): Loading...");
        try {
            URL fontUrl = App.class.getResource(DEFAULT_FONT_PATH);
            if (fontUrl != null) {
                HORROR_FONT = Font.loadFont(fontUrl.toExternalForm(), 20);
                if (HORROR_FONT != null) {
                    HORROR_FONT_FAMILY_NAME = HORROR_FONT.getFamily();
                    System.out.println("App.loadCustomFonts(): Font '" + HORROR_FONT_FAMILY_NAME + "' loaded from " + DEFAULT_FONT_PATH);
                } else { System.err.println("App.loadCustomFonts(): Font.loadFont returned null for " + DEFAULT_FONT_PATH); }
            } else { System.err.println("App.loadCustomFonts(): Font file not found: " + DEFAULT_FONT_PATH); }
        } catch (Exception e) { System.err.println("App.loadCustomFonts(): Error loading font - " + e.getMessage()); e.printStackTrace(); }
        if (HORROR_FONT == null) {
            HORROR_FONT = Font.getDefault(); HORROR_FONT_FAMILY_NAME = HORROR_FONT.getFamily();
            System.err.println("App.loadCustomFonts(): Using system default font.");
        }
    }

    public static void setRoot(String fxmlName, ControllerCallback callback) throws IOException {
        if (appPrimaryStage == null || primaryScene == null) {
            System.err.println("App.setRoot(): Main stage/scene not initialized.");
            throw new IllegalStateException("Stage or Scene not initialized for setRoot");
        }
        System.out.println("App.setRoot(): Attempting to change root to: " + fxmlName + ".fxml");
        String fullFxmlPath = "/com/leave/engine/" + fxmlName + ".fxml";
        
        URL fxmlUrl = App.class.getResource(fullFxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML file not found for setRoot: " + fullFxmlPath);
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setClassLoader(App.class.getClassLoader());
        Parent rootNode = loader.load(); 

        if (rootNode != null) {
            primaryScene.setRoot(rootNode);
            System.out.println("App.setRoot(): Primary scene root changed to: " + fullFxmlPath);
            
            if (callback != null) {
                Object controller = loader.getController();
                if (controller != null) {
                    callback.onControllerLoaded(controller);
                } else {
                    System.err.println("App.setRoot(): Controller for " + fxmlName + " was null after load.");
                }
            }
        } else {
            System.err.println("App.setRoot(): Error setting root. FXML '" + fullFxmlPath + "' did not load to a Parent node.");
            throw new IOException("Could not load FXML to Parent: " + fullFxmlPath);
        }
    }

    @FunctionalInterface
    public interface ControllerCallback {
        void onControllerLoaded(Object controller);
    }

    public static void setRoot(String fxmlName) throws IOException {
        setRoot(fxmlName, null);
    }

    private void showPostLaunchErrorAlert(String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING); // Warning as app is running but degraded
            alert.initOwner(appPrimaryStage);
            alert.setTitle("Application Warning");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    public static void main(String[] args) {
        System.out.println("App.main(): Launching JavaFX application via Application.launch()...");
        launch(args);
    }
}