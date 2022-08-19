package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.*;
import uk.ac.soton.comp1206.event.BlockHoveredListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Current game
     */
    protected Game game;

    //Game metrics labels
    protected Label score = new Label();
    protected Label level = new Label();
    protected Label lives = new Label();
    protected Label multiplier = new Label();

    //Game boards
    protected GameBoard board;
    protected PieceBoard pieceBoard;
    protected PieceBoard followingPieceBoard;

    /**
     * High Score Text
     */
    protected Text highScore;

    /**
     * Timer Bar
     */
    protected TimerBar timerBar;

    /**
     * Main game pane
     */
    protected BorderPane mainPane;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        //Sets up game
        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        //Main pane
        this.mainPane = new BorderPane();
        mainPane.setMaxWidth(gameWindow.getWidth());
        challengePane.getChildren().add(mainPane);

        //Game board
        this.board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        board.setOnRightClicked(this::rightMouseClick);
        //Next piece board
        pieceBoard = new PieceBoard((gameWindow.getWidth() / 10) * 2, (gameWindow.getWidth() / 10) * 2);
        pieceBoard.toggleCentreCircle();
        //Following piece board
        followingPieceBoard = new PieceBoard((gameWindow.getWidth()/8),(gameWindow.getWidth()/8));

        //Set click listeners
        pieceBoard.setOnMouseClicked(this::leftClickPieceBoard);
        followingPieceBoard.setOnMouseClicked(this::clickedFollowingPieceBoard);

        //Current High Score
        var highScoreText = new Text("Highscore: ");
        highScoreText.getStyleClass().add("hiscore");
        this.highScore = new Text(String.valueOf(this.getHighScore()));
        this.highScore.getStyleClass().add("hiscore");

        var highscorePane = new HBox();
        highscorePane.setAlignment(Pos.CENTER_RIGHT);
        highscorePane.getChildren().addAll(highScoreText, highScore);
        highscorePane.setPadding(new Insets(40, 20, 0, 0));

        var upcomingBox = new VBox();
        upcomingBox.setAlignment(Pos.CENTER_RIGHT);
        upcomingBox.setSpacing(50);
        var followingPiecePane = new BorderPane();
        followingPiecePane.setCenter(followingPieceBoard);
        followingPiecePane.setPadding(new Insets(0, 0, 0, 50));
        upcomingBox.getChildren().addAll(pieceBoard, followingPiecePane);

        var boardPane = new BorderPane();
        var piecePane = new BorderPane();
        boardPane.setCenter(board);
        piecePane.setCenter(upcomingBox);

        var statusLeftPane = new BorderPane();
        statusLeftPane.setCenter(piecePane);
        statusLeftPane.setTop(highscorePane);

        mainPane.setLeft(boardPane);
        mainPane.setRight(statusLeftPane);

        BorderPane.setMargin(boardPane, new Insets(0, 0, 0, 50));
        BorderPane.setMargin(piecePane, new Insets(0, 50, 0, 0));

        //Game metrics
        this.score.textProperty().bind(this.game.scoreProperty().asString());
        this.level.textProperty().bind(this.game.levelProperty().asString());
        this.lives.textProperty().bind(this.game.livesProperty().asString());
        this.multiplier.textProperty().bind(this.game.multiplierProperty().asString());

        var scoreLabel = new Label("Score: ");
        scoreLabel.getStyleClass().add("title");
        var levelLabel = new Label("Current Level: ");
        levelLabel.getStyleClass().add("title");
        var livesLabel = new Label("Lives: ");
        livesLabel.getStyleClass().add("title");
        var multiplierLabel = new Label("Multiplier: ");
        multiplierLabel.getStyleClass().add("title");

        this.score.getStyleClass().add("current-game-stats");
        this.level.getStyleClass().add("current-game-stats");
        this.lives.getStyleClass().add("current-game-stats");
        this.multiplier.getStyleClass().add("current-game-stats");

        var scorePane = new FlowPane();
        scorePane.getChildren().addAll(scoreLabel, score);

        var levelPane = new FlowPane();
        levelPane.getChildren().addAll(levelLabel, level);

        var livesPane = new FlowPane();
        livesPane.getChildren().addAll(livesLabel, lives);

        var multiplierPane = new FlowPane();
        multiplierPane.getChildren().addAll(multiplierLabel, multiplier);

        var status1Pane = new HBox();
        status1Pane.setMaxWidth(gameWindow.getWidth());
        status1Pane.setAlignment(Pos.TOP_CENTER);
        status1Pane.getChildren().addAll(scorePane, multiplierPane);

        var status2Pane = new HBox();
        status2Pane.setMaxWidth(gameWindow.getWidth());
        status2Pane.setAlignment(Pos.TOP_CENTER);
        status2Pane.getChildren().addAll(levelPane, livesPane);

        var statusAllPane = new VBox();
        statusAllPane.setMaxWidth(gameWindow.getWidth());
        statusAllPane.setAlignment(Pos.TOP_CENTER);
        statusAllPane.getChildren().addAll(status1Pane, status2Pane);
        statusAllPane.setPadding(new Insets(20, 0, 0, 75));

        mainPane.setTop(statusAllPane);

        //Timer bar
        this.timerBar = new TimerBar(this.game.getTimerDelay(), gameWindow.getWidth()-100, 20);
        var timerBox = new VBox();
        timerBox.setAlignment(Pos.BOTTOM_CENTER);
        timerBox.getChildren().add(timerBar);
        timerBox.setPadding(new Insets(0,0,10,0));
        mainPane.setBottom(timerBox);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnBlockHovered(new BlockHoveredListener() {
            @Override
            public void blockHovered(GameBlock gameBlock) {
                board.setAim(gameBlock.getX(), gameBlock.getY());
            }
            @Override
            public void blockUnHovered(GameBlock gameBlock) {
                gameBlock.setUnselected();
            }
        });
    }

    /**
     * Sets keyboard controls
     * @param event KeyEvent
     */
    protected void keyboardControls(KeyEvent event) {
        if(event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
            this.board.setAim(this.board.getAimX()-1, this.board.getAimY()); //Sets aim left by 1
        } else if(event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
            this.board.setAim(this.board.getAimX()+1, this.board.getAimY()); //Sets aim right by 1
        } else if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
            this.board.setAim(this.board.getAimX(), this.board.getAimY()-1);  //Sets aim up by 1
        } else if(event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
            this.board.setAim(this.board.getAimX(), this.board.getAimY()+1); //Sets aim down by 1
        } else if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.X) {
            this.game.blockClicked(this.board.getBlock(this.board.getAimX(), this.board.getAimY())); //If enter pressed, place block at current aimed position
        } else if(event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.OPEN_BRACKET) {
            this.game.rotateCurrentPiece(3); //Rotate piece 3 times clockwise (1 time anti-clockwise)
        } else if(event.getCode() == KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET) {
            this.game.rotateCurrentPiece(1); //Rotate piece 1 time clockwise
        } else if(event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
            this.game.swapCurrentPiece(); //Swaps next piece and following piece
        } else if(event.getCode() == KeyCode.ESCAPE) {
            this.backToMenu(); //Quits game and returns to menu
        }
    }

    /**
     * Returns to menu
     */
    protected void backToMenu() {
        Multimedia.stopPlayingBackgroundMusic();
        this.game.stopGame();
        this.board = null;
        pieceBoard = null;
        followingPieceBoard = null;
        this.gameWindow.startMenu();
    }

    /**
     * Goes to scores scene
     */
    private void toScores() {
        Multimedia.stopPlayingBackgroundMusic();
        this.game.stopGame();
        this.board = null;
        pieceBoard = null;
        followingPieceBoard = null;
        this.gameWindow.startScores(this.game);
    }

    /**
     * Calculates the current high score
     * @return
     */
    private int getHighScore() {
        //Checks if file exists, otherwise uses current score if greater than the highest default score
        File scoresFile = new File("scores.tetrecs");
        if(!scoresFile.exists()) {
            if(this.game.getScore() > 10000) {
                return this.game.getScore();
            } else {
                return 10000;
            }
        }
        //Otherwise gets the highest score from the scores file
        int score = this.game.getScore();
        try {
            Scanner scanner = new Scanner(scoresFile);
            String line = scanner.nextLine();
            if(line != null) {
                String[] scoreString = line.split(":");
                score = Integer.valueOf(scoreString[1]) > score ? Integer.valueOf(scoreString[1]) : score;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return score;
    }

    /**
     * On right mouse clicked
     * @param event MouseEvent
     */
    private void rightMouseClick(MouseEvent event) {
        if(event.getButton() == MouseButton.SECONDARY) {
            //Rotate piece 1 time clockwise
            this.game.rotateCurrentPiece(1);
        }
    }

    /**
     * On left mouse clicked
     * @param event MouseEvent
     */
    private void leftClickPieceBoard(MouseEvent event) {
        if(event.getButton() == MouseButton.PRIMARY) {
            //Rotate piece 1 time clockwise
            this.game.rotateCurrentPiece(1);
        }
    }

    /**
     * On following piece board clicked
     * @param event MouseEvent
     */
    private void clickedFollowingPieceBoard(MouseEvent event) {
        if(event.getButton() == MouseButton.PRIMARY) {
            //Swaps next and following pieces
            this.game.swapCurrentPiece();
        }
    }


    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clicked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Handle when a line is cleared
     * @param gameBlockCoordinates game blocks needing to be cleared
     */
    private void lineCleared(Set<GameBlockCoordinate> gameBlockCoordinates) {
        //Fades out blocks
        this.board.fadeOut(gameBlockCoordinates);
        //Rechecks high score
        this.highScore.setText(String.valueOf(this.getHighScore()));
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        //Background music
        Multimedia.playBackgroundMusic("/music/game.wav");
        //Key press listener
        this.getScene().setOnKeyPressed(this::keyboardControls);
        this.game.setNextPieceListener((GamePiece nextPiece, GamePiece followingPiece) -> {
            updatePieceBoards(nextPiece, followingPiece);
        });
        this.game.setLineClearedListener(this::lineCleared);
        this.game.setOnGameLoop(new GameLoopListener() {
            @Override
            public void onGameLoop() {
                timerBar.setTime(game.getTimerDelay());
                timerBar.animate();
            }
            @Override
            public void endGame() {
                toScores();
            }
        });
        //Starts game
        game.start();
        //Starts timer bar
        this.timerBar.animate();
    }

    /**
     * Updates piece boards with new pieces
     * @param nextPiece new next piece to display
     * @param followingPiece new following piece to display
     */
    public void updatePieceBoards(GamePiece nextPiece, GamePiece followingPiece) {
        pieceBoard.setPieceToDisplay(nextPiece);
        followingPieceBoard.setPieceToDisplay(followingPiece);
    }

}
