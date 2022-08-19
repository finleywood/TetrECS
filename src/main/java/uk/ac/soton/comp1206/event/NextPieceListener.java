package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Listener for when next piece is called
 */
public interface NextPieceListener {
    /**
     * Controls what happens when next piece is called
     * @param nextPiece next piece given
     * @param followingPiece following piece generated
     */
    void nextPiece(GamePiece nextPiece, GamePiece followingPiece);
}
