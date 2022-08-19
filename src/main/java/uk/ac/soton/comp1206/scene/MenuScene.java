package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);


        //Logo image
        ImageView tetrecsImgView = new ImageView(Multimedia.loadImage("/images/TetrECS.png"));
        tetrecsImgView.setPreserveRatio(true);
        tetrecsImgView.setFitHeight(gameWindow.getHeight()/4);
        tetrecsImgView.setFitWidth((gameWindow.getWidth()/10)*9);
        var imgPane = new BorderPane();
        imgPane.setPadding(new Insets(30));
        imgPane.setCenter(tetrecsImgView);
        imgPane.setMaxWidth(gameWindow.getWidth());
        imgPane.setMaxHeight(gameWindow.getHeight()/2);

        //Rotation animation
        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.setDuration(Duration.millis(3000));
        rotateTransition.setNode(imgPane);
        rotateTransition.setFromAngle(-5);
        rotateTransition.setToAngle(5);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setCycleCount(Timeline.INDEFINITE);

        mainPane.setTop(imgPane);

        //Menu buttons
        var playSingle = new Button("Play Single Player");
        playSingle.getStyleClass().add("menuItem");
        playSingle.setOnAction(this::startGame);
        var playMulti = new Button("Play Multi Player");
        playMulti.setOnAction(this::startLobby);
        playMulti.getStyleClass().add("menuItem");
        var howTo = new Button("How To Play");
        howTo.setOnAction(this::showInstructions);
        howTo.getStyleClass().add("menuItem");
        var exit = new Button("Exit");
        exit.getStyleClass().add("menuItem");
        exit.setOnAction(this::exit);

        var buttonsPane = new VBox();
        buttonsPane.setAlignment(Pos.BOTTOM_CENTER);
        buttonsPane.setPrefWidth(100);
        buttonsPane.setSpacing(15);
        buttonsPane.getStyleClass().add("menu");
        buttonsPane.getChildren().addAll(playSingle, playMulti, howTo, exit);

        mainPane.setCenter(buttonsPane);


        rotateTransition.play();
    }

    /**
     * Handles when escape is pressed
     * @param event given KeyEvent
     */
    private void escPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.ESCAPE) {
            System.exit(0);
        }
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        logger.info("Initialising Menu Scene");
        Multimedia.playBackgroundMusic("/music/menu.mp3");
        this.getScene().setOnKeyPressed(this::escPressed);
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        Multimedia.stopPlayingBackgroundMusic();
        gameWindow.startChallenge();
    }

    /**
     * Goes to Instructions Scene
     * @param event given ActionEvent
     */
    private void showInstructions(ActionEvent event) {
        Multimedia.stopPlayingBackgroundMusic();
        gameWindow.startInstructions();
    }

    /**
     * Starts multiplayer Lobby Scene
     * @param event given ActionEvent
     */
    private void startLobby(ActionEvent event) {
        Multimedia.stopPlayingBackgroundMusic();
        gameWindow.startLobby();
    }

    /**
     * Closes the game
     * @param event
     */
    private void exit(ActionEvent event) {
        System.exit(0);
    }

}
