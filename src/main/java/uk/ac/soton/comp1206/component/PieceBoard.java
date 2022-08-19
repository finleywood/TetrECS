package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * PieceBoard class which extends the GameBoard class
 * An abstraction over the GameBoard for rendering the next and following pieces
 */
public class PieceBoard extends GameBoard {

    /**
     * Show centre circle
     */
    private boolean centreCircle = false;

    /**
     * Create new PieceBoard
     * @param width width of board
     * @param height height of board
     */
    public PieceBoard(int width, int height) {
        super(3, 3, width, height);
    }

    public void setPieceToDisplay(GamePiece pieceToDisplay) {
        this.grid.playPieceBoard(pieceToDisplay, 1, 1);
    }

    public boolean isCentreCircle() {
        return centreCircle;
    }

    /**
     * Toggle's the centre circle on the board
     */
    public void toggleCentreCircle() {
        this.centreCircle = !this.centreCircle;
        if(this.centreCircle) {
            //Always be at 1,1
            this.blocks[1][1].setShowCircle(true);
        }
    }
}
