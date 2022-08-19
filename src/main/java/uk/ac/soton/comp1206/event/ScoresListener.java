package uk.ac.soton.comp1206.event;

/**
 * Listener for when score updates are received
 */
public interface ScoresListener {
    /**
     * Controls what happens on score updates being received from the communicator
     * @param scoresList should be a split array of player and score/lives pairs
     */
    void updateScores(String[] scoresList);
}
