package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

/**
 * MultiplayerScene class extends ChallengeScene class
 * The scene which controls the multiplayer game UI
 */
public class MultiplayerScene extends ChallengeScene {

    /**
     * Server communicator
     */
    private Communicator communicator;

    private Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Leaderboard property
     */
    private SimpleListProperty<Pair<String,Pair<Integer,Boolean>>> leaderBoardList = new SimpleListProperty<Pair<String, Pair<Integer,Boolean>>>();

    //UI elements
    private HBox chatsBox;
    private Text currentChatMessage;
    private VBox boardsBox;

    /**
     * EXTENSION: Map to hold player's game boards
     */
    private HashMap<String, GameBoard> playerBoards = new HashMap<>();

    /**
     * Create a new Multi Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow, ArrayList<String> players, String currentPlayer) {
        super(gameWindow);
        for(String player : players) {
            if(!player.equals(currentPlayer)) {
                playerBoards.put(player, new GameBoard(5, 5, 50, 50));
            }
        }
        this.communicator = gameWindow.getCommunicator();
        this.leaderBoardList.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Pair<Integer,Boolean>>>()));
    }

    @Override
    public void initialise() {
        super.initialise();
        this.communicator.addListener(message -> {
            Platform.runLater(() -> {
                if(message.startsWith("MSG")) {
                    onReceiveMsg(message);
                } else if(message.startsWith("BOARD")) {
                    onReceiveBoard(message);
                } else if(message.startsWith("DIE")) {
                    onPlayerDie(message);
                }
            });
        });
    }

    @Override
    public void build() {
        super.build();
        var multiplayerHeading = new HBox();
        multiplayerHeading.setMaxWidth(gameWindow.getWidth());

        //Leaderboard
        var leaderboard = new Leaderboard();
        this.leaderBoardList.bind(leaderboard.leaderboardScoresProperty());
        leaderboard.setAlignment(Pos.CENTER_RIGHT);

        //New right side box
        var sideBox = new VBox();
        sideBox.setAlignment(Pos.CENTER_RIGHT);
        sideBox.setSpacing(10);
        sideBox.setPadding(new Insets(0, 50, 0, 0));

        this.followingPieceBoard.setPadding(new Insets(0, 25, 0, 0));

        sideBox.getChildren().addAll(leaderboard, this.pieceBoard, this.followingPieceBoard);

        this.mainPane.setRight(sideBox);

        //Current chat box
        this.chatsBox = new HBox();
        this.chatsBox.setAlignment(Pos.BOTTOM_CENTER);
        this.chatsBox.setMaxWidth(this.board.getWidth());

        this.currentChatMessage = new Text("To type a chat message, press T");
        this.currentChatMessage.getStyleClass().add("chatMessage");

        this.chatsBox.getChildren().add(this.currentChatMessage);

        //Box to show game board and current chat
        var boardBox = new VBox();
        boardBox.setAlignment(Pos.CENTER);
        boardBox.setSpacing(5);
        boardBox.getChildren().addAll(this.board, this.chatsBox);

        this.mainPane.setCenter(boardBox);

        //EXTENSION: Side box to show current game boards of other players
        this.boardsBox = new VBox();
        boardsBox.setAlignment(Pos.CENTER_LEFT);
        boardsBox.setSpacing(10);
        boardsBox.setPadding(new Insets(0, 0, 0, 10));
        for(String player : this.playerBoards.keySet()) {
            var playerBoardBox = new VBox();
            var playerText = new Text(player);
            playerText.getStyleClass().add("channelItem");
            playerBoardBox.setSpacing(3);
            playerBoardBox.getChildren().addAll(this.playerBoards.get(player), playerText);
            boardsBox.getChildren().add(playerBoardBox);
        }

        this.mainPane.setLeft(boardsBox);
    }

    @Override
    public void setupGame() {
        logger.info("Setting up Multiplayer game");

        this.game = new MultiplayerGame(5,5, this.communicator);

        this.game.setScoresListener(this::updateScores);
    }

    /**
     * Handle updating scores
     * @param scoresList list of players and respective scores and lives
     */
    private void updateScores(String[] scoresList) {
        logger.info("Updating scores");
        //Add to leaderboard if nothing in it
        if(this.leaderBoardList.size() == 0) {
            for(String playerScoreLine : scoresList) {
                String[] playerScore = playerScoreLine.split(":");
                this.leaderBoardList.add(new Pair<>(playerScore[0], new Pair<Integer,Boolean>(Integer.valueOf(playerScore[1]), playerScore[2].equals("DEAD"))));
            }
        } else {
            //Otherwise, just update player's scores
            for(String playerScoreLine : scoresList) {
                String[] playerScore = playerScoreLine.split(":");
                for(int i = 0; i < this.leaderBoardList.size(); i++) {
                    if(this.leaderBoardList.get(i).getKey().equals(playerScore[0])) {
                        this.leaderBoardList.set(i, new Pair<>(playerScore[0], new Pair<Integer,Boolean>(Integer.valueOf(playerScore[1]),playerScore[2].equals("DEAD"))));
                    }
                }
            }
        }
    }

    @Override
    protected void keyboardControls(KeyEvent event) {
        if(event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
            this.board.setAim(this.board.getAimX()-1, this.board.getAimY());
        } else if(event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
            this.board.setAim(this.board.getAimX()+1, this.board.getAimY());
        } else if(event.getCode() == KeyCode.UP || event.getCode() == KeyCode.W) {
            this.board.setAim(this.board.getAimX(), this.board.getAimY()-1);
        } else if(event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
            this.board.setAim(this.board.getAimX(), this.board.getAimY()+1);
        } else if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.X) {
            this.game.blockClicked(this.board.getBlock(this.board.getAimX(), this.board.getAimY()));
        } else if(event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.OPEN_BRACKET) {
            this.game.rotateCurrentPiece(3);
        } else if(event.getCode() == KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET) {
            this.game.rotateCurrentPiece(1);
        } else if(event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R) {
            this.game.swapCurrentPiece();
        } else if(event.getCode() == KeyCode.ESCAPE) {
            this.backToMenu();
        } else if(event.getCode() == KeyCode.T) { // For new chat message
            TextInputDialog message = new TextInputDialog();
            message.setHeaderText("New Chat Message");
            message.setContentText("Please type your chat message:");
            Optional<String> messageOpt = message.showAndWait();
            if(messageOpt.isPresent()) {
                this.communicator.send("MSG " + messageOpt.get());
            }
        }
    }

    /**
     * Handle a message received
     * @param message message from server
     */
    private void onReceiveMsg(String message) {
        String messageAndPlayer = message.substring(4);
        String[] playerAndMessageArr = messageAndPlayer.split(":");
        this.currentChatMessage.setText("[" + playerAndMessageArr[0] + "] " + playerAndMessageArr[1]);
    }

    /**
     * EXTENSION: Handle a board update received from the server
     * @param message message from server
     */
    private void onReceiveBoard(String message) {
        //Gather player and positions in an array
        String playerAndBoardStr = message.substring(6);
        String[] playerAndBoardDetails = playerAndBoardStr.split(":");
        String player = playerAndBoardDetails[0];
        String boardDetails = playerAndBoardDetails[1];
        String[] boardDetailsIndividual = boardDetails.split(" ");
        //Only update the board is the player has a game board on the left side pane
        if(this.playerBoards.containsKey(player)) {
            GameBoard playerGameBoard = this.playerBoards.get(player);
            int x = 0;
            int y = 0;
            for (String boardDetail : boardDetailsIndividual) {
                playerGameBoard.setBlock(x, y, Integer.valueOf(boardDetail));
                if (y == 4 && x < 4) {
                    y = 0;
                    x++;
                } else {
                    y++;
                }
            }
        }
    }

    /**
     * Handles what happens when a DIE command is received
     * @param message message given by server
     */
    private void onPlayerDie(String message) {
        String player = message.substring(4);
        for(int i = 0; i < this.leaderBoardList.size(); i++) {
            if(this.leaderBoardList.get(i).getKey().equals(player)) {
                //Updates leaderboard
                Pair<String,Pair<Integer,Boolean>> oldPair = this.leaderBoardList.get(i);
                this.leaderBoardList.set(i, new Pair<>(player, new Pair<>(oldPair.getValue().getKey(), true)));
                this.playerBoards.remove(player);
                this.boardsBox.getChildren().clear();
                //Updates player game boards
                for(String playerBoard : this.playerBoards.keySet()) {
                    var playerBoardBox = new VBox();
                    var playerText = new Text(playerBoard);
                    playerText.getStyleClass().add("channelItem");
                    playerBoardBox.setSpacing(3);
                    playerBoardBox.getChildren().addAll(this.playerBoards.get(playerBoard), playerText);
                    boardsBox.getChildren().add(playerBoardBox);
                }
            }
        }
    }
}
