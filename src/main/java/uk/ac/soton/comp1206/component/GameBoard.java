package uk.ac.soton.comp1206.component;

import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.BlockHoveredListener;
import uk.ac.soton.comp1206.game.Grid;

import java.util.Set;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    GameBlock[][] blocks;

    //X and Y aim of selection
    private int aimX = 0;
    private int aimY = 0;

    public int getAimX() {
        return aimX;
    }

    public int getAimY() {
        return aimY;
    }

    public void setAimX(int aimX) {
        this.aimX = aimX;
    }

    public void setAimY(int aimY) {
        this.aimY = aimY;
    }

    /**
     * Sets the current selection aim at the given coordinates
     * @param x x value of block
     * @param y y value of block
     */
    public void setAim(int x, int y) {
        int oldX = this.aimX;
        int oldY = this.aimY;
        if((!(x < 0) && !(y < 0)) && (!(x > this.cols-1) && !(y > this.rows-1))) {
            this.aimX = x;
            this.aimY = y;
            this.blocks[x][y].setSelected();
            this.blocks[oldX][oldY].setUnselected();
        }
    }

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;

    /**
     * Listener to call when a block is hovered over
     */
    private BlockHoveredListener blockHoveredListener;

    public void setOnRightClicked(EventHandler<? super MouseEvent> onRightClickedListener) {
        this.setOnMouseClicked(onRightClickedListener);
    }

    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        //Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols,rows);

        //Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    public void setBlock(int x, int y, int val) {
        this.grid.set(x, y, val);
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}",cols,rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));

        block.setOnMouseEntered((e) -> blockHovered(e, block));
        block.setOnMouseExited((e) -> blockUnHovered(e, block));

        return block;
    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    public void setOnBlockHovered(BlockHoveredListener blockHoveredListener) {
        this.blockHoveredListener = blockHoveredListener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);

        if(blockClickedListener != null && event.getButton() == MouseButton.PRIMARY) {
            blockClickedListener.blockClicked(block);
        }
    }

    /**
     * Calls the block hovered listener when a block is being hovered over
     * @param event MouseEvent
     * @param gameBlock block hovered over
     */
    private void blockHovered(MouseEvent event, GameBlock gameBlock) {
        if(this.blockHoveredListener != null) {
            this.blockHoveredListener.blockHovered(gameBlock);
        }
    }

    /**
     * Calls the block hovered listener when a block has stopped being hovered over
     * @param event
     * @param gameBlock
     */
    private void blockUnHovered(MouseEvent event, GameBlock gameBlock) {
        if(this.blockHoveredListener != null) {
            this.blockHoveredListener.blockUnHovered(gameBlock);
        }
    }

    /**
     * Fades out a set of game blocks
     * @param gameBlockCoordinates Set of coordinates for game blocks to fade out
     */
    public void fadeOut(Set<GameBlockCoordinate> gameBlockCoordinates) {
        for(GameBlockCoordinate gameBlockCoordinate : gameBlockCoordinates) {
            this.blocks[gameBlockCoordinate.getX()][gameBlockCoordinate.getY()].fadeOut();
        }
    }

}
