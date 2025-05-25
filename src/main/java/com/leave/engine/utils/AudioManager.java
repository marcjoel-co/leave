package com.leave.engine.utils; // Or your preferred package

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream; // For MediaPlayer seeking if needed
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioManager {

    private static AudioManager instance;

    private MediaPlayer backgroundMusicPlayer;
    private String currentBGMPath;

    // Cache for sound effect Clips to avoid reloading short sounds repeatedly
    private Map<String, Clip> sfxClipCache;
    private ExecutorService sfxExecutor; // Thread pool for playing SFX

    private float masterVolumeSFX = 1.0f; // 0.0 (mute) to 1.0 (full)
    private double masterVolumeBGM = 1.0;  // 0.0 (mute) to 1.0 (full)

    private Clip currentTextBlipClip; // Special clip for text animation SFX

    private AudioManager() {
        sfxClipCache = new HashMap<>();
        // Using a single thread executor for SFX can prevent too many simultaneous
        // audio lines if many SFX are triggered rapidly, but might queue them.
        // A cached thread pool allows more concurrency but uses more resources if many sounds play.
        // For simple SFX, single thread is often fine and predictable.
        sfxExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true); // So these threads don't prevent JVM exit
            return t;
        });
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    // --- Background Music (BGM) Methods ---

    /**
     * Plays background music. If music is already playing, it stops the current one first.
     * @param resourcePath Classpath resource path to the BGM file (e.g., "/com/leave/audio/level1.mp3")
     * @param loop If true, the music will loop.
     * @param volume Volume from 0.0 to 1.0.
     */
    public void playBackgroundMusic(String resourcePath, boolean loop, double volume) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            System.err.println("AudioManager: BGM resource path is null or empty.");
            return;
        }
        if (currentBGMPath != null && currentBGMPath.equals(resourcePath) && backgroundMusicPlayer != null &&
            backgroundMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            System.out.println("AudioManager: BGM '" + resourcePath + "' is already playing.");
            backgroundMusicPlayer.setVolume(volume * masterVolumeBGM); // Just adjust volume
            return;
        }

        stopBackgroundMusic(); // Stop any current BGM

        try {
            URL bgmUrl = getResourceUrl(resourcePath);
            if (bgmUrl == null) {
                System.err.println("AudioManager: BGM file not found: " + resourcePath);
                return;
            }

            Media media = new Media(bgmUrl.toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(media);
            backgroundMusicPlayer.setVolume(volume * masterVolumeBGM);

            if (loop) {
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } else {
                backgroundMusicPlayer.setOnEndOfMedia(() -> {
                    System.out.println("AudioManager: BGM finished: " + resourcePath);
                    stopBackgroundMusic(); // Release resources
                });
            }
            backgroundMusicPlayer.play();
            currentBGMPath = resourcePath;
            System.out.println("AudioManager: Playing BGM: " + resourcePath);
        } catch (Exception e) {
            System.err.println("AudioManager: Error playing BGM " + resourcePath);
            e.printStackTrace();
            currentBGMPath = null;
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose(); // Release resources
            backgroundMusicPlayer = null;
            System.out.println("AudioManager: BGM stopped.");
        }
        currentBGMPath = null;
    }

    public void setBGMVolume(double volume) {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(Math.max(0.0, Math.min(1.0, volume)) * masterVolumeBGM);
        }
    }

    // --- Sound Effects (SFX) Methods ---

    /**
     * Plays a sound effect once. Uses a cached Clip if available, otherwise loads it.
     * Played on a separate thread.
     * @param resourcePath Classpath resource path to the SFX file (e.g., "/com/leave/audio/click.wav")
     * @param volume       Volume from 0.0f to 1.0f for this SFX.
     */
    public void playSoundEffect(String resourcePath, float volume) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            System.err.println("AudioManager: SFX resource path is null or empty.");
            return;
        }

        sfxExecutor.submit(() -> { // Submit task to the SFX thread pool
            try {
                Clip clip = sfxClipCache.get(resourcePath);

                if (clip == null || !clip.isOpen()) { // Load if not cached or if previous instance was closed
                    URL sfxUrl = getResourceUrl(resourcePath);
                    if (sfxUrl == null) {
                        System.err.println("AudioManager: SFX file not found: " + resourcePath);
                        return;
                    }
                    try (InputStream inputStream = sfxUrl.openStream();
                         BufferedInputStream bis = new BufferedInputStream(inputStream);
                         AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis)) {

                        AudioFormat format = audioStream.getFormat();
                        DataLine.Info info = new DataLine.Info(Clip.class, format);

                        if (!AudioSystem.isLineSupported(info)) {
                            System.err.println("AudioManager: SFX Line not supported for " + resourcePath);
                            return;
                        }
                        clip = (Clip) AudioSystem.getLine(info);
                        clip.open(audioStream);
                        sfxClipCache.put(resourcePath, clip); // Cache the opened clip
                        System.out.println("AudioManager: SFX loaded and cached: " + resourcePath);
                    }
                } else {
                     // System.out.println("AudioManager: Using cached SFX: " + resourcePath);
                }


                // Set volume for this playback instance
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    // Convert linear volume (0-1) to decibels. Max gain is often 6.0206f, min can be -80f or lower
                    // This is a simplified conversion. Real dB conversion is logarithmic.
                    float minDb = gainControl.getMinimum();
                    float maxDb = gainControl.getMaximum();
                    float range = maxDb - minDb;
                    // For very low volumes, MASTER_GAIN expects very negative values.
                    // A simple linear scale mapping might not be perceptually linear.
                    // This approximation aims for lower values being quieter.
                    // Example: if volume is 0.1, gain becomes minDb + 0.1*range
                    float effectiveVolume = Math.max(0.0f, Math.min(1.0f, volume * masterVolumeSFX));
                    if (effectiveVolume == 0.0f) {
                        gainControl.setValue(minDb); // Mute
                    } else {
                        // Simplified mapping: use portion of range above a very low value
                        // This is a rough approximation for perceived loudness
                        float db = (float) (Math.log10(effectiveVolume) * 20.0);
                        db = Math.max(minDb, Math.min(maxDb, db)); // Clamp to valid range
                        gainControl.setValue(db);

                        // If the above is too complex or doesn't sound right, a simpler approach:
                        // float targetDb = minDb + (effectiveVolume * range);
                        // gainControl.setValue(targetDb);
                    }
                }

                // Ensure clip is at the beginning if reusing
                clip.setFramePosition(0);
                clip.start();

                // No LineListener to close here, as we want to reuse the Clip from cache.
                // Clips are closed when a new Clip is loaded for the same path, or via a cleanup method.

            } catch (Exception e) {
                System.err.println("AudioManager: Error playing SFX " + resourcePath);
                e.printStackTrace();
            }
        });
    }

    public void playSoundEffect(String resourcePath) {
        playSoundEffect(resourcePath, 1.0f); // Default full volume for this effect
    }

    /**
     * Preloads sound effects into the cache. Call during loading screens.
     * @param resourcePaths Varargs of SFX resource paths to load.
     */
    public void preloadSoundEffects(String... resourcePaths) {
        for (String path : resourcePaths) {
            if (path == null || path.trim().isEmpty() || sfxClipCache.containsKey(path)) continue;
            sfxExecutor.submit(() -> { // Preload on SFX thread
                try {
                     URL sfxUrl = getResourceUrl(path);
                    if (sfxUrl == null) return;
                    try (InputStream inputStream = sfxUrl.openStream();
                         BufferedInputStream bis = new BufferedInputStream(inputStream);
                         AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis)) {
                        Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioStream.getFormat()));
                        clip.open(audioStream);
                        sfxClipCache.put(path, clip);
                        System.out.println("AudioManager: Preloaded SFX: " + path);
                    }
                } catch (Exception e) {
                    System.err.println("AudioManager: Error preloading SFX " + path);
                    e.printStackTrace();
                }
            });
        }
    }


    // --- Text Animation SFX Methods (Specialized) ---
    // For text blips, it's often better to have a dedicated, preloaded clip
    // and just restart it rapidly.

    /**
     * Prepares the text blip sound.
     * @param resourcePath Path to the short WAV file for text blips.
     */
    public void loadTextBlipSound(String resourcePath) {
         if (resourcePath == null || resourcePath.trim().isEmpty()) {
            System.err.println("AudioManager: Text blip resource path is null or empty.");
            return;
        }
        try {
            currentTextBlipClip = sfxClipCache.get(resourcePath); // Try cache first
            if (currentTextBlipClip == null || !currentTextBlipClip.isOpen()) {
                URL sfxUrl = getResourceUrl(resourcePath);
                if (sfxUrl == null) throw new IOException("Text blip sound not found: " + resourcePath);

                try (InputStream inputStream = sfxUrl.openStream();
                     BufferedInputStream bis = new BufferedInputStream(inputStream);
                     AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis)) {

                    currentTextBlipClip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioStream.getFormat()));
                    currentTextBlipClip.open(audioStream);
                    sfxClipCache.put(resourcePath, currentTextBlipClip); // Also cache it
                    System.out.println("AudioManager: Text blip sound loaded: " + resourcePath);
                }
            }
            // Set default volume for text blip if needed (can be lower)
             if (currentTextBlipClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) currentTextBlipClip.getControl(FloatControl.Type.MASTER_GAIN);
                // Example: set to -10dB for a quieter blip, adjust as needed
                // You might want a separate volume control for text blips
                float db = (float) (Math.log10(0.3 * masterVolumeSFX) * 20.0); // 0.3 for 30% volume
                db = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), db));
                gainControl.setValue(db);
            }

        } catch (Exception e) {
            System.err.println("AudioManager: Error loading text blip sound " + resourcePath);
            e.printStackTrace();
            currentTextBlipClip = null;
        }
    }

    /**
     * Plays the preloaded text blip sound. Very low latency targeted.
     */
    public void playTextBlip() {
        if (currentTextBlipClip != null && currentTextBlipClip.isOpen()) {
            // Don't run this on the sfxExecutor, play directly for lowest latency
            // as AnimationTimer for text rendering is on JavaFX thread.
            // Rapid calls can be an issue, but Clips are good at restarting.
            currentTextBlipClip.setFramePosition(0); // Rewind
            currentTextBlipClip.start();             // Play
        } else {
            // System.err.println("AudioManager: Text blip sound not loaded or ready.");
        }
    }


    // --- Master Volume Controls ---
    public void setMasterSFXVolume(float volume) {
        this.masterVolumeSFX = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("AudioManager: Master SFX Volume set to " + this.masterVolumeSFX);
        // Note: This doesn't change volume of currently playing cached SFX clips directly.
        // Volume for SFX is set at the time of playback.
        // For text blip, you might re-apply volume:
        if (currentTextBlipClip != null && currentTextBlipClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) currentTextBlipClip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = (float) (Math.log10(0.3 * masterVolumeSFX) * 20.0);
            db = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), db));
            gainControl.setValue(db);
        }
    }

    public void setMasterBGMVolume(double volume) {
        this.masterVolumeBGM = Math.max(0.0, Math.min(1.0, volume));
        if (backgroundMusicPlayer != null) {
            // BGM volume is usually set on MediaPlayer as (baseVolume * masterVolume)
            // So we need the original baseVolume it was played with if we want to scale.
            // For simplicity, this just re-applies based on master, assuming BGM player was started with relative volume 1.0.
            // A better approach: store intended base volume for current BGM.
            backgroundMusicPlayer.setVolume(this.masterVolumeBGM); // Simpler: sets MediaPlayer volume directly
             System.out.println("AudioManager: Master BGM Volume set to " + this.masterVolumeBGM);
        }
    }

    // --- Utility and Cleanup ---
    private URL getResourceUrl(String resourcePath) {
        URL url = AudioManager.class.getResource(resourcePath);
        if (url == null && !resourcePath.startsWith("/")) {
            url = AudioManager.class.getResource("/" + resourcePath);
        }
        return url;
    }

    /**
     * Call this when application is shutting down to release resources.
     */
    public void shutdown() {
        System.out.println("AudioManager: Shutting down...");
        stopBackgroundMusic();
        if (currentTextBlipClip != null && currentTextBlipClip.isOpen()) {
            currentTextBlipClip.close();
        }
        for (Clip clip : sfxClipCache.values()) {
            if (clip.isOpen()) {
                clip.close();
            }
        }
        sfxClipCache.clear();
        sfxExecutor.shutdown(); // Gracefully shut down the SFX thread pool
        System.out.println("AudioManager: Shutdown complete.");
    }
}