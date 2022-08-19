package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Checks if a piece can be played
     * @param gamePiece game piece to be played
     * @param x x position requested
     * @param y y position requested
     * @return boolean
     */
    public boolean canPlayPiece(GamePiece gamePiece, int x, int y) {
        //Return false if centre position is in use
        int[][] blocks = gamePiece.getBlocks();
        if (this.grid[x][y].get() != 0) {
            return false;
        }
        //Checks whether pieces around the centre can be played
        boolean pieceCanBePlayed = true;
        for(int col = 0; col < blocks.length; col++) {
            for(int row = 0; row < blocks[col].length; row++) {
                if(blocks[col][row] != 0) {
                    int colDiff = col - 1;
                    int rowDiff = row - 1;
                    int xOffset = x + colDiff;
                    int yOffset = y + rowDiff;
                    if (xOffset < 0 || yOffset < 0 || xOffset >= this.cols || yOffset >= this.rows) {
                        pieceCanBePlayed = false;
                    } else if (this.grid[xOffset][yOffset].get() != 0) {
                        pieceCanBePlayed = false;
                    }
                }
            }
        }
        return pieceCanBePlayed;
    }

    /**
     * Plays a piece
     * @param gamePiece piece to be played
     * @param x x position
     * @param y y position
     */
    public void playPiece(GamePiece gamePiece, int x, int y) {
        //Plays piece at x,y from centre
        int[][] blocks = gamePiece.getBlocks();
        for(int col = 0; col < blocks.length; col++) {
            for (int row = 0; row < blocks[col].length; row++) {
                if(blocks[col][row] != 0) {
                    int colDiff = col - 1;
                    int rowDiff = row - 1;
                    int xOffset = x + colDiff;
                    int yOffset = y + rowDiff;
                    this.grid[xOffset][yOffset].set(blocks[col][row]);
                }
            }
        }
    }

    /**
     * Plays piece on a pieceBoard
     * @param gamePiece piece to play
     * @param x x position
     * @param y y position
     */
    public void playPieceBoard(GamePiece gamePiece, int x, int y) {
        //Plays piece at x,y from centre
        int[][] blocks = gamePiece.getBlocks();
        for(int col = 0; col < blocks.length; col++) {
            for (int row = 0; row < blocks[col].length; row++) {
                int colDiff = col - 1;
                int rowDiff = row - 1;
                int xOffset = x + colDiff;
                int yOffset = y + rowDiff;
                this.grid[xOffset][yOffset].set(blocks[col][row]);
            }
        }
    }

}