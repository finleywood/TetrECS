package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * InstructionsScene class extends from BaseScene class
 * Scene to show instructions on how to play the game
 */
public class InstructionsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Create a new instructions scene
     * @param gameWindow current game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    @Override
    public void initialise() {
        logger.info("Initialising Instructions Scene");
        Multimedia.playBackgroundMusic("/music/menu.mp3");
        this.getScene().setOnKeyPressed(this::escPressed);
    }

    @Override
    public void build() {
        this.root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var instructionsPane = new StackPane();
        instructionsPane.setMaxWidth(gameWindow.getWidth());
        instructionsPane.setMaxHeight(gameWindow.getHeight());
        instructionsPane.getStyleClass().add("menu-background");
        this.root.getChildren().add(instructionsPane);

        var mainPane = new BorderPane();
        instructionsPane.getChildren().add(mainPane);

        //Instructions image
        Image instructions = Multimedia.loadImage("/images/Instructions.png");
        ImageView imgView = new ImageView(instructions);
        imgView.setPreserveRatio(true);
        imgView.setFitWidth(gameWindow.getWidth()-50);
        imgView.setFitHeight(gameWindow.getHeight()-50);

        mainPane.setCenter(imgView);

        //Title
        Text title = new Text("Instructions (Esc to exit)");
        title.getStyleClass().add("heading");
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.TOP_CENTER);
        titleBox.setMaxWidth(gameWindow.getWidth());
        titleBox.getChildren().add(title);
        titleBox.setPadding(new Insets(20, 0, 0, 0));
        mainPane.setTop(titleBox);

        var piecesTitle = new Text("Game Pieces:");
        piecesTitle.getStyleClass().add("heading");

        //Dynamically generated game pieces
        var pieceGridPane = new GridPane();
        pieceGridPane.setHgap(1);
        pieceGridPane.setPrefSize(100, gameWindow.getWidth());
        pieceGridPane.setPadding(new Insets(0, 0, 0, 20));
        for(int i = 0; i < 15; i++) {
            PieceBoard pieceBoard = new PieceBoard(50,50);
            GamePiece piece = GamePiece.createPiece(i);
            pieceBoard.setPieceToDisplay(piece);
            pieceGridPane.add(pieceBoard, i, 0);
        }

        var piecesBox = new VBox(piecesTitle, pieceGridPane);
        piecesBox.setSpacing(5);
        piecesBox.setMaxWidth(gameWindow.getWidth());
        piecesBox.setAlignment(Pos.BOTTOM_CENTER);


        mainPane.setBottom(piecesBox);
    }

    /**
     * Handle Escape being pressed
     * @param event given KeyEvent
     */
    private void escPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.ESCAPE) {
            Multimedia.stopPlayingBackgroundMusic();
            gameWindow.startMenu();
        }
    }
}
