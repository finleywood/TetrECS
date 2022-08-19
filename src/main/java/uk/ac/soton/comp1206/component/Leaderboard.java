package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.ArrayList;


/**
 * Leaderboard class which extends the ScoresList class
 * It provides an abstraction over the score list for holding and displaying leaderboard items when in a multiplayer match
 * The scene should bind to the simple list property to update the leaderboard automatically
 */
public class Leaderboard extends ScoresList {

    /**
     * Simple list property for leaderboard scores and if dead
     */
    private SimpleListProperty<Pair<String,Pair<Integer,Boolean>>> leaderboardScores = new SimpleListProperty<>();

    /**
     * Create a new leaderboard
     */
    public Leaderboard() {
        super(false, true, "Leaderboard:");

        this.leaderboardScores.set(FXCollections.observableArrayList(new ArrayList<Pair<String,Pair<Integer,Boolean>>>()));
        this.leaderboardScores.addListener(this::updateLeaderboardScoresList);
    }

    /**
     * Listener to update leaderboard ui component
     * @param observable observable list value
     * @param oldValue old list
     * @param newValue new, updated list
     */
    protected void updateLeaderboardScoresList(ObservableValue<? extends ObservableList<Pair<String, Pair<Integer,Boolean>>>> observable, ObservableList<Pair<String, Pair<Integer,Boolean>>> oldValue, ObservableList<Pair<String, Pair<Integer,Boolean>>> newValue) {
        this.renderLeaderboardScores(newValue);
    }

    /**
     * Renders the leaderboard scores to the UI
     * @param scores the new scores to render
     */
    protected void renderLeaderboardScores(ObservableList<Pair<String, Pair<Integer,Boolean>>> scores) {
        //Heading
        this.getChildren().clear();
        Text heading = new Text(this.heading);
        heading.getStyleClass().add("heading");
        this.getChildren().add(heading);
        //Render every score
        for(Pair<String,Pair<Integer,Boolean>> score : scores) {
            Text text = new Text(score.getKey() + ": " + score.getValue().getKey());
            text.getStyleClass().add("scoreitem");
            //Cross out if dead
            text.setStrikethrough(score.getValue().getValue());
            this.getChildren().add(text);
            //Animate reveal
            this.reveal(text);
        }
    }

    /**
     * Returns the list property to bind to
     * @return list property to bind to
     */
    public ListProperty leaderboardScoresProperty() {
        return this.leaderboardScores;
    }
}
