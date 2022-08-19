package uk.ac.soton.comp1206.component;

import javafx.animation.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TimerBar class extends the javafx Rectangle class
 * Provides an easy-to-use component for the life loss timer
 */
public class TimerBar extends Rectangle {
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Deafult width of bar
     */
    private double defaultWidth;

    /**
     * Time bar should take to collapse
     */
    private long time;

    /**
     * Create a new TimerBar
     * @param time starting time taken to collapse bar
     * @param width width of bar
     * @param height height of bar
     */
    public TimerBar(long time, double width, double height) {
        super(0, 0, width, height);

        this.defaultWidth = width;

        this.time = time;

        this.setFill(Color.GREEN);
    }

    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Public method to animate the bar collapsing
     */
    public void animate() {
        //Reset bar
        this.setFill(Color.GREEN);
        this.setWidth(this.defaultWidth);

        //Timeline to animate and interpolate bar collapsing and change colour
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(this.widthProperty(), 0, Interpolator.LINEAR);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(this.time), keyValue);
        KeyFrame yellow = new KeyFrame(Duration.millis((this.time/10)*4), new KeyValue(this.fillProperty(), Color.YELLOW));
        KeyFrame red = new KeyFrame(Duration.millis((this.time/10)*8), new KeyValue(this.fillProperty(), Color.RED));
        timeline.setCycleCount(1);
        timeline.getKeyFrames().addAll(keyFrame, yellow, red);
        timeline.play();
    }
}
