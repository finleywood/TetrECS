package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.ScoresListener;
import uk.ac.soton.comp1206.scene.ChallengeScene;

import java.util.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    protected final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * Next game piece
     */
    protected GamePiece currentPiece;
    /**
     * Following game piece
     */
    protected GamePiece followingPiece;

    //Integer properties for game metrics
    protected SimpleIntegerProperty score = new SimpleIntegerProperty(0);
    protected SimpleIntegerProperty level = new SimpleIntegerProperty(0);
    protected SimpleIntegerProperty lives = new SimpleIntegerProperty(3);
    protected SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * Game loop timer
     */
    protected Timer gameLoopTimer;

    //Listeners
    protected NextPieceListener nextPieceListener;
    protected LineClearedListener lineClearedListener;
    protected GameLoopListener gameLoopListener;
    protected ScoresListener scoresListener;

    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }

    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    public void setScoresListener(ScoresListener scoresListener) {
        this.scoresListener = scoresListener;
    }

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        spawnStartPieces();

        //Run a Game loop timer task every timer delay
        this.gameLoopTimer = new Timer();
        gameLoopTimer.schedule(new GameLoopTimeTask(), getTimerDelay());
    }

    /**
     * Spawns starting game pieces
     */
    protected void spawnStartPieces() {
        this.currentPiece = this.spawnPiece();
        this.followingPiece = this.spawnPiece();
        this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
    }

    /**
     * Custom timer task to run the game loop and schedule a new task at the end of each cycle with the updated delay
     */
    protected class GameLoopTimeTask extends TimerTask {
        @Override
        public void run() {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    gameLoop();
                }
            });
            gameLoopTimer.schedule(new GameLoopTimeTask(), getTimerDelay());
        }
    };

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if(this.grid.canPlayPiece(this.currentPiece, x, y)) {
            logger.info("Playing piece " + this.currentPiece + " at " + x + ", " + y);
            Multimedia.playAudio("/sounds/place.wav");
            this.grid.playPiece(this.currentPiece, x, y);

            this.afterPiece();

            this.nextPiece();
        } else {
            Multimedia.playAudio("/sounds/fail.wav");
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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

    public GamePiece spawnPiece() {
        Random random = new Random(System.currentTimeMillis());
        int piece = random.nextInt(15);
        return GamePiece.createPiece(piece);
    }

    /**
     * Gets the next game piece from follwing piece
     * Then spawns a new following piece
     */
    protected void nextPiece() {
        this.currentPiece = this.followingPiece;
        this.followingPiece = this.spawnPiece();
        this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
    }

    /**
     * Code to run after a piece is placed
     */
    public void afterPiece() {
        //Checks if lines can be cleared
        this.checkClearLines();

        //If the floor of the score divided by 1000 is greater than the current level, increase the level (ie. every 1000 points gained is 1 level reached)
        if(Math.floor(this.score.get() / 1000) > this.level.get()) {
            this.increaseLevel((int)Math.floor(this.score.get() / 1000));
        }

        //Reset gameLoop timer
        this.resetTimer();
    }

    /**
     * Checks if lines can be cleared
     */
    private void checkClearLines() {
        ArrayList<Integer> rowsToClear = new ArrayList<Integer>();
        ArrayList<Integer> colsToClear = new ArrayList<Integer>();
        //Adds row and column numbers to array lists if all of a row or col is greater than 0
        for(int row = 0; row < this.rows; row++) {
            boolean clearRow = true;
            for(int col = 0; col < this.cols; col++) {
                if(this.grid.get(col, row) == 0) {
                    clearRow = false;
                }
            }
            if(clearRow) {
                rowsToClear.add(row);
            }
        }
        for(int col = 0; col < this.cols; col++) {
            boolean clearCol = true;
            for(int row = 0; row < this.rows; row++) {
                if(this.grid.get(col, row) == 0) {
                    clearCol = false;
                }
            }
            if(clearCol) {
                colsToClear.add(col);
            }
        }
        //Gathers game block coordinates for all blocks being cleared
        HashSet<GameBlockCoordinate> gameBlockCoordinates = new HashSet<GameBlockCoordinate>();
        int numOfLines = rowsToClear.size() + colsToClear.size();
        //Calculates number of blocks cleared
        int numOfBlocks = ((rowsToClear.size() * this.cols) + (colsToClear.size() * this.rows)) - ((rowsToClear.size() > 0 && colsToClear.size() > 0) ? colsToClear.size() : 0);
        //Clears rows and columns
        for(int row : rowsToClear) {
            for(int col = 0; col < this.cols; col++) {
                this.grid.set(col, row, 0);
                gameBlockCoordinates.add(new GameBlockCoordinate(col, row));
            }
        }
        for(int col : colsToClear) {
            for(int row = 0; row < this.rows; row++) {
                this.grid.set(col, row, 0);
                boolean addGameBlockCoordinate = true;
                for(Iterator<GameBlockCoordinate> gameBlockCoordinateIterator = gameBlockCoordinates.iterator(); gameBlockCoordinateIterator.hasNext(); ) {
                    GameBlockCoordinate gameBlockCoordinate = gameBlockCoordinateIterator.next();
                    if(gameBlockCoordinate.getX() == col && gameBlockCoordinate.getY() == row) {
                        addGameBlockCoordinate = false;
                    }
                }
                if(addGameBlockCoordinate) {
                    gameBlockCoordinates.add(new GameBlockCoordinate(col, row));
                }
            }
        }

        //Activates line cleared listener on blocks cleared
        this.lineClearedListener.lineCleared(gameBlockCoordinates);

        //Adds to score and multiplier if lines are cleared
        this.score(numOfLines, numOfBlocks);
        if(colsToClear.size() > 0 || rowsToClear.size() > 0) {
            Multimedia.playAudio("/sounds/clear.wav");
            this.increaseMultiplier();
        } else {
            this.resetMultiplier();
        }
    }

    public int getScore() {
        return this.score.get();
    }
    public void setScore(int score) {
        this.score.set(score);
    }

    public int getLevel() {
        return this.level.get();
    }
    public void setLevel(int level) {
        this.level.set(level);
    }

    public int getLives() {
        return this.lives.get();
    }
    public void setLives(int lives) {
        this.lives.set(lives);
    }

    public int getMultiplier() {
        return this.multiplier.get();
    }
    public void setMultiplier(int multiplier) {
        this.multiplier.set(multiplier);
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public IntegerProperty livesProperty() {
        return lives;
    }

    public IntegerProperty multiplierProperty() {
        return multiplier;
    }

    protected void score(int numOfLines, int numOfBlocks) {
        this.score.set(this.score.get() + (numOfLines * numOfBlocks * 10 * this.multiplier.get()));
    }

    /**
     * Increases multiplier
     */
    private void increaseMultiplier() {
        this.multiplier.set(this.multiplier.get() + 1);
    }

    /**
     * Resets multiplier to 1
     */
    private void resetMultiplier() {
        this.multiplier.set(1);
    }

    /**
     * Changes the level to the level given
     * @param level new level
     */
    private void increaseLevel(int level) {
        this.level.set(level);
    }

    /**
     * Rotates the current piece by rotations given clockwise
     * @param rotations
     */
    public void rotateCurrentPiece(int rotations) {
        Multimedia.playAudio("/sounds/rotate.wav");
        this.currentPiece.rotate(rotations);
        this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
    }

    /**
     * Swaps the current piece and following piece
     */
    public void swapCurrentPiece() {
        Multimedia.playAudio("/sounds/transition.wav");
        GamePiece currentPieceOld = this.currentPiece;
        this.currentPiece = this.followingPiece;
        this.followingPiece = currentPieceOld;
        this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
    }

    /**
     * Calculates the timer delay for the gameLoop
     * @return
     */
    public long getTimerDelay() {
        if((12000 - (this.getLevel() * 500)) < 2500) {
            return 2500;
        }
        return (12000 - (this.getLevel() * 500));
    }

    /**
     * Restarts the gameLoop timer
     */
    private void resetTimer() {
        this.gameLoopTimer.cancel();
        this.gameLoopTimer = new Timer();
        this.gameLoopTimer.schedule(new GameLoopTimeTask(), getTimerDelay());
        this.gameLoopListener.onGameLoop();
    }

    /**
     * Game Loop
     */
    private void gameLoop() {
        if(this.getLives() == 0) {
            this.gameLoopListener.endGame();
        } else {
            Multimedia.playAudio("/sounds/lifelose.wav");
            this.setLives(this.getLives() - 1);
            this.resetMultiplier();
            this.nextPiece();
            this.gameLoopListener.onGameLoop();
        }
    }

    /**
     * Stops the game loop
     */
    public void stopGame() {
        this.gameLoopTimer.cancel();
    }
}
