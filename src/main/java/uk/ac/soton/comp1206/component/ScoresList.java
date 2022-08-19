package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * ScoresList extends the VBox javafx class
 * A simple UI component to hold/update scores and render to the scene
 */
public class ScoresList extends VBox {
    /**
     * Scores property
     */
    private SimpleListProperty<Pair<String,Integer>> scores = new SimpleListProperty();
    //Properties for rendering
    private boolean online;
    private boolean leaderboard;
    protected String heading;

    /**
     * Create a new ScoresList object
     * @param online is the scores list online
     * @param leaderboard is the scores list a leaderboard
     * @param heading what heading should the list have
     */
    public ScoresList(boolean online, boolean leaderboard, String heading) {
        this.leaderboard = leaderboard;
        this.online = online;
        this.heading = heading;
        this.scores.set(FXCollections.observableArrayList(new ArrayList<Pair<String, Integer>>()));
        this.scores.addListener(this::updateScoresList);
        this.setAlignment(Pos.BOTTOM_LEFT);
    }

    /**
     * Create an empty scores list object with no heading
     */
    public ScoresList() {
        ObservableList<Pair<String,Integer>> observableList = FXCollections.observableArrayList(new ArrayList<Pair<String,Integer>>());
        this.scores.set(observableList);
        this.scores.addListener(this::updateScoresList);
        this.setAlignment(Pos.BOTTOM_LEFT);
    }

    /**
     * Listener to list property
     * @param observable observable value
     * @param oldValue old list
     * @param newValue new list
     */
    protected void updateScoresList(ObservableValue<? extends ObservableList<Pair<String,Integer>>> observable, ObservableList<Pair<String,Integer>> oldValue, ObservableList<Pair<String,Integer>> newValue) {
        this.renderScores(newValue);
    }

    /**
     * Renders the scores for the ScoresList
     * @param scores new scores to render
     */
    protected void renderScores(ObservableList<Pair<String,Integer>> scores) {
        //Heading
        this.getChildren().clear();
        Text heading = new Text(this.heading);
        heading.getStyleClass().add("heading");
        this.getChildren().add(heading);
        for(Pair<String,Integer> score : scores) {
            Text text = new Text(score.getKey() + " " + score.getValue());
            text.getStyleClass().add("scoreitem");
            this.getChildren().add(text);
            //Reveal each score
            this.reveal(text);
        }
    }

    /**
     * Animate a text component to reveal slowly
     * @param text text component to reveal
     */
    protected void reveal(Text text) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(2000));
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.setNode(text);
        fadeTransition.play();
    }

    /**
     * Returns scores list property
     * @return scores list property
     */
    public ListProperty<Pair<String,Integer>> scoresProperty() {
        return this.scores;
    }
}
