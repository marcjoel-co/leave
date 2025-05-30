# The Night Shift - A Dynamic Choose Your Own Adventure Engine

[![Java Version](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
[![JavaFX Version](https://img.shields.io/badge/JavaFX-13%2B-orange.svg)](https://openjfx.io/)
[![Maven Build](https://img.shields.io/badge/Build-Maven-brightgreen.svg)](https://maven.apache.org/)

"The Night Shift" is a data-driven Choose Your Own Adventure (CYOA) semi game engine developed using JavaFX for the graphical user interface and Maven for project management similar to Undertale! The engine is designed to dynamically load interactive narratives from JSON files, offering a flexible framework for creating visual adventure games. It features scene rendering with backgrounds and character sprites, interactive dialogue, player choices that shape the story, and state management.

## ✨ Core Features

* **📖 Data-Driven Storytelling:** Game narratives, including scenes, dialogue, character appearances, choices, and asset paths, are defined externally in JSON files. This allows for easy content creation and modification without altering Java code.
* **🖼️ Visual Scene Rendering:** Supports static background images and animated background spritesheets.
* **🧍 Character Representation:** Displays on-screen character sprites (both static single-frame images and animated spritesheets) and dedicated dialogue portraits for speakers.
* **💬 Interactive Dialogue System:** Presents dialogue line-by-line with optional typewriter text animation, speaker identification, and character-specific portraits.
* **🧭 Player Agency:** Player choices directly influence the progression of the story, leading to different paths and multiple endings.
* **🧠 State Management:** The engine tracks game state, including the current scene, player's chosen character name, story flags, and (planned) inventory items, enabling conditional logic in the narrative.
* **🎶 Audiovisual Experience:** Incorporates background music and sound effects to enhance immersion.
* **🛠️ Modular Architecture:** Built with JavaFX for the GUI, leveraging FXML for UI layout and CSS for styling. Jackson library handles JSON deserialization.

## 📂 Project Structure

* **`src/main/java`**: All Java code for the engine and application logic.
* **`src/main/resources`**: All assets required by the application at runtime. The `com/leave/engine` subdirectory helps keep resources organized consistently with the Java package structure, which is good practice for `getResourceAsStream`.
* **`pom.xml`**: The Maven configuration file, defining dependencies (JavaFX modules, Jackson, etc.) and build plugins (like `javafx-maven-plugin`).
* **`module-info.java`**: Defines module requirements and exports for use with JPMS, enhancing encapsulation and reliability.

Key Feature

* **`App.java`**: The main entry point. Initializes JavaFX, creates the primary stage, loads the initial FXML (`gameEntry.fxml`), and handles high-level scene transitions using `App.setRoot()`. It also initializes singletons like `GameManager` and `AudioManager` at startup.
* **`GameManager.java`**: A singleton serving as the central "brain" of the game.
* Loads the `GameStory` from `sao.json` via `StoryLoader`.
* Manages crucial game state: `currentSceneId`, `currentPlayerName`, inventory, story flags, `gameOver` status.
* Provides methods like `getCurrentSceneData()`, `advanceToScene()`, `makeChoice()`, `processAction()`, and `processText()` (for placeholder replacement) that `GamePlayController` uses.
* **`GamePlayController.java`**: Controller for `gameplay.fxml`.
* Its `displayCurrentScene()` method is responsible for rendering all visual elements based on `SceneData` from `GameManager`.
* `showNextDialogueLine()` orchestrates dialogue presentation: fetching lines, managing speaker portraits (`speakerPortraitImageView`) and names (`speakerNameLabel`), and using `AnimationUtils` for text effects.
* `populateAndShowChoices()` dynamically creates and displays interactive `Button`s in `choicesVBox` based on the current scene's choices, including conditional checks for `requiredItem`/`requiredFlag`.
* `processEndOfSceneLogic()` correctly determines whether to show choices, auto-transition, trigger an outcome, or display an ending scene's terminal buttons after dialogue completion.
* Manages UI views (dialogue vs. choices) using a `StackPane` (`dialogueAndChoicesStack`).
* **`MainMenuController.java`**: Controller for `gameEntry.fxml`. Handles initial logo animation and allows the player to select a character. Upon starting a new game, it configures `GameManager` with the player's choice and initiates the transition to the gameplay scene.
* **JSON Story Structure (`sao.json` & POJOs):**
* The game's narrative is entirely defined in `sao.json`.
* This JSON is mapped to Java objects (POJOs like `GameStory`, `SceneData`, `DialogueEntry`, `ChoiceData`, `OutcomeData`, `SpriteInfo`, `CharacterSpriteInfo`) using the Jackson library via `StoryLoader`.
* `DialogueEntry` includes a `portraitPath` for speaker-specific dialogue portraits.
* `SceneData` supports `backgroundImage` (for static) and `backgroundSprite` (`SpriteInfo` for animated/single-frame spritesheets), as well as `characterSprite` (`CharacterSpriteInfo` for on-screen full character) and a list of `ChoiceData`. Ending scenes can also have an `endingTitle`.
* `ChoiceData` can have `requiredItem` and `requiredFlag` for conditional availability.
* `OutcomeData` can have a `nextSceneId` to transition to a dedicated ending scene after an outcome message.
* **`SpriteSheetAnimator.java`**: A utility class that handles rendering of both animated sprite sheets and single static images (treated as 1-frame sprite sheets) by manipulating the `viewport` of an `ImageView`.
* **`AudioManager.java`**: A singleton that loads and plays sound effects (`AudioClip`) and background music (`MediaPlayer`).

## 🚀 Getting Started

### Prerequisites  

* Java Development Kit (JDK) 11 or newer.
* Apache Maven 3.6.x or newer.
* JavaFX SDK (version compatible with your JDK, e.g., JavaFX 17+ for JDK 17). Dependencies are typically managed by Maven in `pom.xml`.

> all of these came with the default config when creating a maven project using VSC

## 🎨 Styling and Assets

* **UI Layout:** FXML files (`gameEntry.fxml`, `gameplay.fxml`) define the structure of the GUI.
* **Styling:** `style.css` contains CSS rules to customize the appearance of JavaFX components.
* **Fonts:** Custom fonts like "Le Mano" are included in the resources.
* **Images & Audio:** Located in `src/main/resources/com/leave/engine/images` and `src/main/resources/com/leave/engine/data/audio` respectively.
* **Team** Assets were populated by Hera, Denver Eijel and Pocholo while Joel was incharge of loading in the app

## 💡 Key Design Principles

* **Data-Driven Design:** Maximizes flexibility by separating story content from engine code.
* **MVC-like Pattern:** FXML (View), Controllers (Controller logic), GameManager/POJOs (Model/Data).
* **Modularity:** Utilizes Java modules (JPMS) and separates concerns into distinct classes and packages.
* **Single Responsibility (Attempted):** Each class aims to manage a specific aspect of the game (e.g., `AudioManager` for audio, `SpriteSheetAnimator` for sprite rendering).

## 🔮 Future Enhancements

* **Create a proper mainmenu system** unhard code the main menu system
* **Save/Load Game State:** Allow players to save and resume their progress.
* **Inventory System:** Implement item collection and usage affecting choices and puzzles.
* **More Complex Flag Logic:** Introduce more intricate conditional branching based on a wider array of player actions and story flags.
* **Enhanced Animations:** More sophisticated visual transitions and effects.
* **Settings Menu:** Options for volume control, text speed, etc.
* **Internationalization (i18n):** Support for multiple languages in the story JSON.