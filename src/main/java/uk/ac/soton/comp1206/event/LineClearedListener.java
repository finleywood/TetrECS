package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * Listener for when a line is cleared
 */
public interface LineClearedListener {
    /**
     * Controls what happens when a line is cleared
     * @param gameBlockCoordinates set of coordinates of game blocks cleared
     */
    void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinates);
}
