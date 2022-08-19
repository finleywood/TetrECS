package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.*;

/**
 * ScoresScene class extends from BaseScene class
 * Scene renders the high scores to the UI
 */
public class ScoresScene extends BaseScene {
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Current held player name
     */
    private String currentName;

    /**
     * Whether the game is a multiplayer game or not
     */
    private boolean isMultiplayer;

    /**
     * Final game object
     */
    private final Game game;

    /**
     * Server communicator
     */
    private final Communicator communicator;

    /**
     * Local scores list property
     */
    private SimpleListProperty<Pair<String,Integer>> localScores = new SimpleListProperty<Pair<String,Integer>>();

    /**
     * Remote scores list property
     */
    private SimpleListProperty<Pair<String,Integer>> remoteScores = new SimpleListProperty<Pair<String,Integer>>();

    /**
     * Timer to go back to menu
     */
    private Timer closeTimer;

    /**
     * Create a new ScoresScene object
     * @param gameWindow current game window
     * @param game final game object
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        logger.info("Creating Scores Scene");
        this.game = game;
        this.communicator = gameWindow.getCommunicator();

        //If the game object is an instance of the MultiplayerGame class set isMultiplayer true
        this.isMultiplayer = game instanceof MultiplayerGame;

        ArrayList<Pair<String,Integer>> localScoresArrList = new ArrayList<Pair<String,Integer>>();
        ObservableList<Pair<String,Integer>> observableArrayList = FXCollections.observableArrayList(localScoresArrList);
        this.localScores.set(observableArrayList);

        ObservableList<Pair<String,Integer>> remoteScoresList = FXCollections.observableArrayList(new ArrayList<Pair<String,Integer>>());
        this.remoteScores.set(remoteScoresList);
    }

    @Override
    public void initialise() {
        logger.info("Initialising Scores Scene");
        gameWindow.getScene().setOnKeyPressed(this::escPressed);
        //If the game is not multiplayer, render local scores
        if(!this.isMultiplayer) {
            this.loadScores();
            this.writeLocalScore();
        } else {
            //Otherwise, show the game scores in the local scores place
            MultiplayerGame multiGame = (MultiplayerGame) this.game;
            for(String key : multiGame.getPlayerScores().keySet()) {
                this.localScores.add(new Pair<>(key, multiGame.getPlayerScores().get(key)));
            }
        }
        this.loadOnlineScores();
        this.communicator.addListener(new CommunicationsListener() {
            @Override
            public void receiveCommunication(String communication) {
                Platform.runLater(() -> {
                    //Handle high scores received from server
                    String[] linesSplit = communication.split("\n");
                    String[] firstLineSplit = linesSplit[0].split(" ");
                    if(firstLineSplit[0].equals("HISCORES")) {
                        if(firstLineSplit[1].length() > 0) {
                            String[] scoresPairStr = firstLineSplit[1].split(":");
                            remoteScores.add(new Pair<String,Integer>(scoresPairStr[0], Integer.valueOf(scoresPairStr[1])));
                            for(int i = 1; i < linesSplit.length; i++) {
                                String line = linesSplit[i];
                                if(line != null) {
                                    String[] scorePairStr = line.split(":");
                                    remoteScores.add(new Pair<String,Integer>(scorePairStr[0], Integer.valueOf(scorePairStr[1])));
                                }
                            }
                        }
                        writeOnlineScore();
                    }
                });
            }
        });
        //Back to menu timer after 10 seconds
        this.closeTimer = new Timer();
        this.closeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    Multimedia.stopPlayingBackgroundMusic();
                    gameWindow.startMenu();
                });
            }
        }, 10000);
    }

    @Override
    public void build() {
        this.root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var scoresPane = new StackPane();
        scoresPane.setMaxHeight(gameWindow.getHeight());
        scoresPane.setMaxWidth(gameWindow.getWidth());
        scoresPane.getStyleClass().add("menu-background");
        this.root.getChildren().add(scoresPane);

        var mainPane = new BorderPane();
        scoresPane.getChildren().add(mainPane);

        //Score lists
        ScoresList localList = new ScoresList(false, false, this.isMultiplayer ? "Game High Scores:" : "Local High Scores:");
        ScoresList remoteList = new ScoresList(true, false, "Online High Scores");
        this.remoteScoresProperty().bind(remoteList.scoresProperty());
        this.localScoresProperty().bind(localList.scoresProperty());
        var scoreListsPane = new HBox();
        scoreListsPane.setAlignment(Pos.BOTTOM_CENTER);
        scoreListsPane.setMaxWidth(gameWindow.getWidth());
        scoreListsPane.setPadding(new Insets(0, 50, 20, 50));
        scoreListsPane.getChildren().addAll(localList, remoteList);
        scoreListsPane.setSpacing(200);
        mainPane.setBottom(scoreListsPane);

        //Logo image
        ImageView tetrecsImgView = new ImageView(Multimedia.loadImage("/images/TetrECS.png"));
        tetrecsImgView.setPreserveRatio(true);
        tetrecsImgView.setFitHeight(gameWindow.getHeight()/4);
        tetrecsImgView.setFitWidth((gameWindow.getWidth()/10)*9);

        //Game over text
        var gameOverText = new Text("Game Over");
        gameOverText.getStyleClass().add("bigtitle");

        var headingBox = new VBox();
        headingBox.setAlignment(Pos.TOP_CENTER);
        headingBox.setSpacing(25);
        headingBox.setMaxWidth(gameWindow.getWidth());
        headingBox.getChildren().addAll(tetrecsImgView, gameOverText);
        headingBox.setPadding(new Insets(20, 0, 0, 0));

        mainPane.setTop(headingBox);

    }

    /**
     * Writes updated local scores
     */
    private void writeLocalScore() {
        boolean inserted = false;
        for(int i = 0; i < this.localScores.size(); i++) {
            if(this.game.getScore() > this.localScores.get(i).getValue() && !inserted) {
                if(currentName == null) {
                    //Gets name if not already gotten
                    TextInputDialog nameCapture = new TextInputDialog();
                    nameCapture.setTitle("Name Capture");
                    nameCapture.setHeaderText("Congrats!");
                    int pos = i + 1;
                    nameCapture.setContentText("You have beat the position " + pos + " score! Please provide us with your name:");
                    Optional<String> result = nameCapture.showAndWait();
                    String name = "Player";
                    if (result.isPresent()) {
                        name = result.get();
                    }
                    this.currentName = name;
                }
                //Sets local score at right position
                this.localScores.set(i, new Pair<String,Integer>(this.currentName, this.game.getScore()));
                inserted = true;
            }
        }
        //If updated, write scores to file
        if(inserted){
            this.writeScores(this.localScores.get());
        }
    }

    /**
     * Writes online high score, if required
     */
    private void writeOnlineScore() {
        boolean inserted = false;
        for(int i = 0; i < this.remoteScores.size(); i++) {
            if(this.game.getScore() > this.remoteScores.get(i).getValue() && !inserted) {
                logger.info("Online high score beat!");
                if(this.currentName == null) {
                    //Gets name if not already gotten
                    TextInputDialog nameCapture = new TextInputDialog();
                    nameCapture.setTitle("Name Capture");
                    nameCapture.setHeaderText("Congrats!");
                    int pos = i + 1;
                    nameCapture.setContentText("You have beat the position " + pos + " score! Please provide us with your name:");
                    Optional<String> result = nameCapture.showAndWait();
                    String name = "Player";
                    if (result.isPresent()) {
                        name = result.get();
                    }
                    this.currentName = name;
                }
                //Sends new high score to server
                this.remoteScores.set(i, new Pair<String,Integer>(this.currentName, this.game.getScore()));
                this.communicator.send("HISCORE " + this.currentName + ":" + this.game.getScore());
                inserted = true;
            }
        }
    }

    /**
     * Writes a list of default generated high scores to a file if it doesn't exist
     */
    private void writeDefaultScores() {
        File scoresFile = new File("scores.tetrecs");
        try {
            //Creates file and writes from 10000 to 1000 with name Finley, every 1000
            scoresFile.createNewFile();
            try (FileWriter fw = new FileWriter(scoresFile);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw)) {
                for(int i = 10000;i > 0; i-=1000) {
                    pw.println("Finley:" + i);
                }
            }

        } catch (IOException e) {
            logger.error("Error writing default scores");
            e.printStackTrace();
        }
    }

    /**
     * Loads local scores from file
     */
    public void loadScores() {
        try {
            File scoresFile = new File("scores.tetrecs");
            if(!scoresFile.exists()) {
                //Writes default scores to a file if file doesn't exist
                this.writeDefaultScores();
            }
            //Read scores from file and adds to localScores property
            Scanner scanner = new Scanner(scoresFile);
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line != null) {
                    String[] scorePair = line.split(":");
                    Pair<String, Integer> pair = new Pair<String, Integer>(scorePair[0], Integer.valueOf(scorePair[1]));
                    logger.info(line);
                    this.localScores.add(pair);
                }
            }
            scanner.close();
        } catch (IOException ex) {
            logger.info("An error occurred in reading the scores file");
            ex.printStackTrace();
        }
    }

    /**
     * Writes a list of new scores to a file
     * @param newScores list of new scores
     */
    public void writeScores(ObservableList<Pair<String,Integer>> newScores) {
        File scoresFile = new File("scores.tetrecs");
        try {
            //Creates file if it does not exist
            scoresFile.createNewFile();
        } catch (IOException e) {
            logger.error("Error opening scores file");
            e.printStackTrace();
        }
        //Writes each line to a file called 'scores.tetrecs'
        try (FileWriter fw = new FileWriter(scoresFile, false);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter pw = new PrintWriter(bw)) {
            for(Pair<String,Integer> newScore : newScores) {
                pw.println(newScore.getKey() + ":" + newScore.getValue());
            }
        } catch (IOException e) {
            logger.error("Error writing to scores file");
            e.printStackTrace();
        }
    }

    /**
     * Sends HISCORES command to server
     */
    private void loadOnlineScores() {
        this.communicator.send("HISCORES");
    }

    /**
     * Handle escape pressed
     * @param event given KeyEvent
     */
    private void escPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.ESCAPE) {
            this.closeTimer.cancel();
            Multimedia.stopPlayingBackgroundMusic();
            gameWindow.startMenu();
        }
    }

    /**
     * Returns local scores list property
     * @return current localScores ListProperty
     */
    public ListProperty localScoresProperty() {
        return this.localScores;
    }

    /**
     * Returns remote scores list property
     * @return current remoteScores list property
     */
    public ListProperty remoteScoresProperty() {
        return this.remoteScores;
    }
}
