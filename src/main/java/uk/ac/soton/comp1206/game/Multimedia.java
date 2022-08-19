package uk.ac.soton.comp1206.game;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Objects;

/**
 * Multimedia class to handle multimedia events
 */
public class Multimedia {
    /**
     * Static audio player
     */
    protected static MediaPlayer audioPlayer;
    /**
     * Static music player
     */
    protected static MediaPlayer musicPlayer;

    /**
     * Plays an audio file once
     * @param file file to play
     */
    public static void playAudio(String file) {
        Media media = new Media(getItemPath(file));
        audioPlayer = new MediaPlayer(media);
        audioPlayer.play();
    }


    /**
     * Retrieves the file path for a given file resources
     * @param file required file
     * @return full file path
     */
    private static String getItemPath(String file) {
        return Objects.requireNonNull(Multimedia.class.getResource(file).toExternalForm());
    }

    /**
     * Plays background music on request
     * @param file file of background music
     */
    public static void playBackgroundMusic(String file) {
        Media media = new Media(getItemPath(file));
        musicPlayer = new MediaPlayer(media);
        musicPlayer.setAutoPlay(true);
        //Creates a loop
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.play();
    }

    /**
     * Stops currently playing background music
     */
    public static void stopPlayingBackgroundMusic() {
        musicPlayer.stop();
    }

    /**
     * Loads an image from given file
     * @param file location of image
     * @return  Image
     */
    public static Image loadImage(String file) {
        Image img = new Image(getItemPath(file));
        return img;
    }

}
