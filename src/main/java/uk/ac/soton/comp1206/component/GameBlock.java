package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * Boolean value which decides whether an aim circle is drawn
     */
    private boolean showCircle = false;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setGlobalAlpha(0.2);
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0, width, height);
        gc.setGlobalAlpha(1);

        //Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillPolygon(new double[]{0,width,0}, new double[]{0,0,height}, 3);

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(0.3);

        gc.setEffect(colorAdjust);
        gc.fillPolygon(new double[]{0,width,width}, new double[]{height,height,0},3);

        gc.setEffect(null);

        //Paints a circle if required
        if(this.showCircle) {
            gc.setGlobalAlpha(0.5);
            gc.setFill(Color.GREY);
            gc.fillOval(width/4, height/4, width/2, height/2);
            gc.setGlobalAlpha(1);
        }

        //Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Animates the clearing of a block
     */
    public void fadeOut() {
        AnimationTimer timer = new AnimationTimer() {
            boolean animationStarted;
            @Override
            public void handle(long now) {
                var gc = getGraphicsContext2D();
                gc.clearRect(0,0,width,height);
                if(!animationStarted) {
                    //Initialising
                    gc.setGlobalAlpha(0.5);
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0,0,width,height);
                    gc.setGlobalAlpha(1);
                    gc.setStroke(Color.WHITE);
                    gc.strokeRect(0,0,width,height);
                    gc.setGlobalAlpha(0.5);
                    animationStarted = true;
                } else if(gc.getGlobalAlpha() > 0) {
                    //If transparency hasn't reached 0
                    gc.setGlobalAlpha(gc.getGlobalAlpha()-0.05);
                    gc.setFill(Color.WHITE);
                    gc.fillRect(0,0,width,height);
                    double oldGlobalAlpha = gc.getGlobalAlpha();
                    gc.setGlobalAlpha(1);
                    gc.setStroke(Color.WHITE);
                    gc.strokeRect(0,0,width,height);
                    gc.setGlobalAlpha(oldGlobalAlpha);
                } else {
                    //Reset
                    gc.setGlobalAlpha(1);
                    paintEmpty();
                    this.stop();
                }
            }
        };
        timer.start();
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Sets the block as selected
     */
    public void setSelected() {
        if(value.get() == 0) {
            var gc = getGraphicsContext2D();
            //Clear
            gc.clearRect(0,0,width,height);

            //Fill
            gc.setFill(Color.GREY);
            gc.fillRect(0,0, width, height);

            //Border
            gc.setStroke(Color.WHITE);
            gc.strokeRect(0,0,width,height);
        } else {
            //Set's opacity to 0.6 if block is painted a colour
            this.setOpacity(0.6);
        }
    }

    /**
     * Sets the block as unselected
     */
    public void setUnselected() {
        if(value.get() == 0) {
            this.paintEmpty();
        } else {
            this.setOpacity(1);
        }
    }

    public boolean isShowCircle() {
        return showCircle;
    }

    public void setShowCircle(boolean showCircle) {
        this.showCircle = showCircle;
        //Paint circle
        paint();
    }
}
