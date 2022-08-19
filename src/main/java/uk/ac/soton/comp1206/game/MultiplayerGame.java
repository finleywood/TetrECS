package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Multiplayer game class which extends from the Game class
 * For use in multiplayer game sessions
 */
public class MultiplayerGame extends Game {

    /**
     * Upcoming pieces queue from server
     */
    private LinkedList<GamePiece> piecesQueue = new LinkedList<GamePiece>();

    /**
     * Server communicator
     */
    private Communicator communicator;

    /**
     * Current scores hash map
     */
    private HashMap<String, Integer> playerScores = new HashMap<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     * @param communicator current server communicator
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);

        this.communicator = communicator;
        this.communicator.addListener(message -> {
            Platform.runLater(() -> {
                if(message.startsWith("PIECE")) {
                    onNewPiece(message);
                } else if(message.startsWith("SCORES")) {
                    updateScores(message.substring(7).split("\n"));
                }
            });
        });

        this.communicator.send("SCORES");
    }

    @Override
    protected void spawnStartPieces() {
        this.communicator.send("PIECE");
        this.communicator.send("PIECE");
        this.communicator.send("PIECE");
        this.communicator.send("PIECE");
    }

    /**
     * Method to update current scores
     * @param scoresList array of list of new scores
     */
    private void updateScores(String[] scoresList) {
        for(String scoreStr : scoresList) {
            String[] playerAndScore = scoreStr.split(":");
            if(this.playerScores.containsKey(playerAndScore[0])) {
                this.playerScores.replace(playerAndScore[0], Integer.valueOf(playerAndScore[1]));
            } else {
                this.playerScores.put(playerAndScore[0], Integer.valueOf(playerAndScore[1]));
            }
        }
        this.scoresListener.updateScores(scoresList);
    }

    /**
     * What to do when piece is received from the server
     * @param message
     */
    private void onNewPiece(String message) {
        String piece = message.substring(6);
        GamePiece nextPiece = GamePiece.createPiece(Integer.valueOf(piece));
        if(this.currentPiece == null) {
            this.currentPiece = nextPiece;
        } else if(this.followingPiece == null) {
            this.followingPiece = nextPiece;
            this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
        } else {
            this.piecesQueue.add(nextPiece);
        }
    }

    @Override
    protected void nextPiece() {
        this.currentPiece = this.followingPiece;
        this.followingPiece = this.piecesQueue.remove();
        this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
        this.communicator.send("PIECE");
    }

    @Override
    public void setLives(int lives) {
        super.setLives(lives);
        this.communicator.send("LIVES " + this.getLives());
    }

    @Override
    protected void score(int numOfLines, int numOfBlocks) {
        super.score(numOfLines, numOfBlocks);
        this.communicator.send("SCORE " + this.getScore());
    }

    @Override
    public void afterPiece() {
        super.afterPiece();
        StringBuilder str = new StringBuilder("BOARD");
        for(int col = 0; col < this.grid.getCols(); col++) {
            for(int row = 0; row < this.grid.getRows(); row++) {
                str.append(" " + this.grid.get(col, row));
            }
        }
        this.communicator.send(str.toString());
    }

    @Override
    public void stopGame() {
        this.communicator.send("DIE");
        super.stopGame();
    }

    public HashMap<String, Integer> getPlayerScores() {
        return playerScores;
    }
}
