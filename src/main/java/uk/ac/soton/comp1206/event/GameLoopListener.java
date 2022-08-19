package uk.ac.soton.comp1206.event;

/**
 * Listener to control what happens when the gameLoop is activated
 */
public interface GameLoopListener {
    /**
     * Code that runs on gameLoop running
     */
    void onGameLoop();

    /**
     * Controls what happens when the end of the game is reached
     */
    void endGame();
}
