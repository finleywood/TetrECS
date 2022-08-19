package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Channel;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

/**
 * LobbyScene class extends BaseScene class
 * LobbyScene to join channels for the multiplayer game mode
 */
public class LobbyScene extends BaseScene {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Server communicator
     */
    private final Communicator communicator;

    /**
     * Current connected channel
     */
    private Channel currentChannel;

    /**
     * Current players in channel
     */
    private SimpleSetProperty<String> channelPlayers = new SimpleSetProperty<String>();

    //Channel elements
    private VBox channelBox;
    private HBox newChannelNameBox;
    private TextField newChannelEntry;
    private BorderPane mainPane;

    private VBox currentChannelBox;
    private HBox channelPlayersBox;
    private VBox currentMessagesBox;
    private Button startBtn;
    private TextField messageEntry;
    private Text channelNameText;

    /**
     * Timer to check current channels available
     */
    private Timer timer;

    /**
     * Create a LobbyScene
     * @param gameWindow current game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        this.channelPlayers.set(FXCollections.observableSet(new HashSet<String>()));
        this.currentChannel = new Channel();
        this.communicator = gameWindow.getCommunicator();
    }

    @Override
    public void initialise() {
        this.timer = new Timer();
        //Check for channels every 3 seconds
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        communicator.send("LIST");
                    }
                });
            }
        }, 3000, 3000);

        //Listener to deal with messages received by server
        communicator.addListener(message -> {
            Platform.runLater(() -> {
                if(message.startsWith("CHANNELS")) {
                    renderChannels(message);
                } else if(message.startsWith("JOIN")) {
                    joinChannel(message);
                } else if(message.startsWith("HOST")) {
                    hostCommand();
                } else if(message.startsWith("NICK")) {
                    nickCommand(message);
                } else if(message.startsWith("USERS")) {
                    usersCommand(message);
                } else if(message.startsWith("ERROR")) {
                    onErrorCommand(message);
                } else if(message.startsWith("MSG")) {
                    onNewMsg(message);
                } else if(message.startsWith("PARTED")) {
                    onParted();
                } else if(message.startsWith("START")) {
                    startMultiplayerGame();
                } else if(message.startsWith("DIE")) {
                    onDie(message);
                }
            });
        });

        this.communicator.send("LIST");

        gameWindow.getScene().setOnKeyPressed(this::onEscPressed);

        Multimedia.playBackgroundMusic("/music/menu.mp3");
    }

    @Override
    public void build() {
        this.root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var lobbyPane = new StackPane();
        lobbyPane.setMaxWidth(gameWindow.getWidth());
        lobbyPane.setMaxHeight(gameWindow.getHeight());
        lobbyPane.getStyleClass().add("menu-background");
        root.getChildren().add(lobbyPane);

        this.mainPane = new BorderPane();
        lobbyPane.getChildren().add(mainPane);

        //Title
        var titleText = new Text("Multiplayer");
        titleText.getStyleClass().add("lobby-title");
        var titleBox = new HBox();
        titleBox.setMaxWidth(gameWindow.getWidth());
        titleBox.setAlignment(Pos.TOP_CENTER);
        titleBox.setPadding(new Insets(20, 0, 0, 0));
        titleBox.getChildren().add(titleText);
        mainPane.setTop(titleBox);


        //Channels box
        this.channelBox = new VBox();
        this.channelBox.setMaxHeight(gameWindow.getHeight());
        var channelHeader = new Text("Channels");
        channelHeader.getStyleClass().add("heading");
        var newChannel = new Button("Create A Channel");
        newChannel.setOnAction(this::toggleNewChannelBox);
        newChannel.getStyleClass().add("channelCreateBtn");
        var channelOverallBox = new VBox();
        channelOverallBox.setAlignment(Pos.TOP_LEFT);
        channelOverallBox.setMaxHeight(gameWindow.getHeight());
        channelOverallBox.setPadding(new Insets(10));
        channelOverallBox.getStyleClass().add("channel");
        channelOverallBox.setSpacing(10);

        //Channel creation UI
        this.newChannelNameBox = new HBox();
        this.newChannelEntry = new TextField();
        var channelCreateBtn = new Button("Ok");
        channelCreateBtn.setOnAction(this::newChannel);
        this.newChannelNameBox.setSpacing(5);
        this.newChannelNameBox.getChildren().addAll(this.newChannelEntry, channelCreateBtn);
        this.newChannelNameBox.setVisible(false);

        channelOverallBox.getChildren().addAll(channelHeader, newChannel, newChannelNameBox, this.channelBox);
        var channelsPaddedPane = new BorderPane();
        channelsPaddedPane.setPadding(new Insets(0, 0, 20, 20));
        channelsPaddedPane.setCenter(channelOverallBox);
        mainPane.setLeft(channelsPaddedPane);

        //Joined channel box
        this.currentChannelBox = new VBox();
        currentChannelBox.setSpacing(5);
        currentChannelBox.setPadding(new Insets(0,20,0,0));
        currentChannelBox.setAlignment(Pos.TOP_RIGHT);
        currentChannelBox.setMaxHeight(gameWindow.getHeight());
        currentChannelBox.setMaxWidth(gameWindow.getWidth()/2);

        this.channelNameText = new Text("Channel: " + this.currentChannel.getName());
        channelNameText.getStyleClass().add("heading");
        currentChannelBox.getChildren().add(channelNameText);

        //Players in current channel
        this.channelPlayersBox = new HBox();
        this.channelPlayersBox.setMaxWidth(gameWindow.getWidth());
        this.channelPlayersBox.setSpacing(10);

        for(String player : this.currentChannel.getPlayers()) {
            logger.info(player);
            Text text = new Text(player);
            text.getStyleClass().add("channelItem");
            channelPlayersBox.getChildren().add(text);
        }
        channelPlayersBox.getChildren().add(new Text("Players:"));
        this.channelPlayers.bind(this.currentChannel.playersProperty());
        this.channelPlayers.addListener(this::playersChangedListener);
        currentChannelBox.getChildren().add(this.channelPlayersBox);

        var messagePane = new BorderPane();
        messagePane.setPrefWidth(gameWindow.getWidth()/2);
        messagePane.setPrefHeight(gameWindow.getHeight()/2);
        messagePane.getStyleClass().add("channel");

        //Messages
        var currentMessagesPane = new ScrollPane();
        currentMessagesPane.getStyleClass().add("messages");
        currentMessagesPane.setPrefHeight(messagePane.getHeight()-100);
        currentMessagesPane.setPrefWidth(messagePane.getWidth());

        this.currentMessagesBox = new VBox();
        this.currentMessagesBox.setPrefWidth(currentMessagesPane.getPrefWidth());
        this.currentMessagesBox.setPrefHeight(currentMessagesPane.getPrefHeight());
        VBox.setVgrow(this.currentMessagesBox, Priority.ALWAYS);
        this.currentMessagesBox.setSpacing(5);
        this.currentMessagesBox.getChildren().add(new Text("Type /nick <Name> to change current Nick Name"));
        var spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        spacer.setPrefHeight(20);
        this.currentMessagesBox.getChildren().add(spacer);

        currentMessagesPane.setContent(this.currentMessagesBox);

        messagePane.setCenter(currentMessagesPane);

        var chatBoxBox = new HBox();
        this.messageEntry = new TextField();
        this.messageEntry.setOnKeyPressed(this::sendMessage);
        chatBoxBox.getChildren().add(messageEntry);

        //Action buttons while in channel
        var buttonsBox = new HBox();
        buttonsBox.setSpacing(20);
        var leaveBtn = new Button("Leave Channel");
        leaveBtn.setOnAction(this::leaveChannelAction);
        this.startBtn = new Button("Start Game");
        this.startBtn.setOnAction(this::startGame);
        startBtn.setVisible(this.currentChannel.isHost());

        buttonsBox.getChildren().addAll(leaveBtn, startBtn);

        currentChannelBox.getChildren().addAll(messagePane, chatBoxBox, buttonsBox);

        //Default to not visible, as not in channel when scene loads
        this.currentChannelBox.setVisible(false);

        this.mainPane.setRight(currentChannelBox);


    }

    /**
     * Shows channels received from server on screen
     * @param message message from server
     */
    public void renderChannels(String message) {
        this.channelBox.getChildren().clear();
        String channels = message.substring(9);
        String[] channelListStr = channels.split("\n");
        logger.info("Channels received and registered!");
        for(String channel : channelListStr) {
            Text channelText = new Text(channel);
            channelText.getStyleClass().add("channelItem");
            channelText.setOnMouseClicked(this::sendJoinCommand);
            this.channelBox.getChildren().add(channelText);
        }
    }

    /**
     * Handle Escape being pressed
     * @param event
     */
    private void onEscPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.ESCAPE) {
            leaveChannel();
            Multimedia.stopPlayingBackgroundMusic();
            this.stopTimer();
            gameWindow.startMenu();
        }
    }

    /**
     * Handle DIE messages from the server
     * @param message message from server
     */
    private void onDie(String message) {
        String user = message.substring(4);
        this.currentChannel.removePlayer(user);
    }

    /**
     * Leave a channel
     * @param event given ActionEvent
     */
    private void leaveChannelAction(ActionEvent event) {
        this.leaveChannel();
    }

    /**
     * Resets the current channel and sends a PART command, if a channel if joined
     */
    private void leaveChannel() {
        if(!this.currentChannel.isParted()) {
            this.communicator.send("PART");
            this.currentChannel.leaveChannel();
        }
    }

    /**
     * Sends a start command to the server
     * @param event given ActionEvent
     */
    private void startGame(ActionEvent event) {
        this.communicator.send("START");
    }

    /**
     * Sends a message to the server
     * @param event given KeyEvent
     */
    private void sendMessage(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            String message = this.messageEntry.getText();
            if(message != null) {
                //If a /nick command, then send a NICK command to server
                if(message.startsWith("/nick")) {
                    String newNick = message.substring(6);
                    this.communicator.send("NICK " + newNick);
                } else {
                    //Otherwise send as a normal message
                    this.communicator.send("MSG " + message);
                }
                this.messageEntry.clear();
            }
        }
    }

    /**
     * Handle when a channel is parted
     */
    private void onParted() {
        this.startBtn.setVisible(false);
        this.currentChannelBox.setVisible(false);
    }

    /**
     * Listener for when players in the channel change
     * @param observable observable value
     * @param oldValue old set
     * @param newValue new set
     */
    public void playersChangedListener(ObservableValue<? extends ObservableSet<String>> observable, ObservableSet<String> oldValue, ObservableSet<String> newValue) {
        this.channelPlayersBox.getChildren().clear();
        for(String player : this.currentChannel.getPlayers()) {
            Text text = new Text(player);
            text.getStyleClass().add("channelItem");
            channelPlayersBox.getChildren().add(text);
        }
    }

    /**
     * Handle when a channel is joined
     * @param message message from the server
     */
    public void joinChannel(String message) {
        //Resets channel name
        String channelName = message.substring(5);
        this.currentChannel.joinChannel(channelName);
        this.channelNameText.setText("Channel: " + channelName);
        //Resets messages box
        this.currentMessagesBox.getChildren().clear();
        var spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinWidth(Region.USE_PREF_SIZE);
        spacer.setPrefHeight(20);
        var nickInfoText = new Text("Type /nick <Name> to change current Nick Name");
        this.currentMessagesBox.getChildren().addAll(nickInfoText, spacer);
        //Sets channel box visible
        this.currentChannelBox.setVisible(true);
    }

    /**
     * Sets current player as host when host command is received
     */
    public void hostCommand() {
        this.currentChannel.setHost(true);
        this.startBtn.setVisible(true);
    }

    /**
     * Handle a NICK command being received
     * @param message message received from server
     */
    public void nickCommand(String message) {
        if(!message.contains(":")) {
            String playerNickname = message.substring(5);
            this.currentChannel.setPlayerNickName(playerNickname);
        } else {
            String messageSubStr = message.substring(5);
            String[] nicksOldNew = messageSubStr.split(":");
            this.currentChannel.updatePlayerName(nicksOldNew[0], nicksOldNew[1]);
        }
    }

    /**
     * Handle a USERS command from the server
     * @param message message sent from server
     */
    public void usersCommand(String message) {
        String usersStrSubStr = message.substring(6);
        String[] players = usersStrSubStr.split("\n");
        HashSet<String> playersSet = new HashSet<String>();
        for(String player : players) {
            playersSet.add(player);
        }
        this.currentChannel.setPlayers(playersSet);
    }

    /**
     * Sends a join command for a given channel
     * @param event
     */
    public void sendJoinCommand(MouseEvent event) {
        if(event.getButton() == MouseButton.PRIMARY) {
            //Get node clicked and text as channel name
            Text source = (Text)event.getSource();
            String channelName = source.getText();
            this.communicator.send("JOIN " + channelName);
        }
    }

    /**
     * Handle an ERROR received
     * @param message message received from server
     */
    public void onErrorCommand(String message) {
        String errMsg = message.substring(6);
        //Show error alert
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(errMsg);
        alert.showAndWait();
    }

    /**
     * Handle when a MSG is received from the server
     * @param message
     */
    private void onNewMsg(String message) {
        String messageSubStr = message.substring(4);
        String[] playerAndMessage = messageSubStr.split(":");
        Text messageText = new Text("[" + playerAndMessage[0] + "] " + playerAndMessage[1]);
        this.currentMessagesBox.getChildren().add(messageText);
    }

    /**
     * New Channel input toggle
     * @param event given ActionEvent
     */
    public void toggleNewChannelBox(ActionEvent event) {
        this.newChannelNameBox.setVisible(!this.newChannelNameBox.isVisible());
    }

    /**
     * Create a new channel
     * @param event given ActionEvent
     */
    public void newChannel(ActionEvent event) {
        String name = this.newChannelEntry.getText();
        if(name != null) {
            communicator.send("CREATE " + name);
            var channel = new Text(name);
            channel.getStyleClass().add("channelItem");
            this.channelBox.getChildren().add(channel);
        }
    }

    /**
     * Start a game when a START command is received
     */
    private void startMultiplayerGame() {
        ArrayList<String> players = new ArrayList<>();
        for(String player : this.channelPlayers) {
            players.add(player);
        }
        Multimedia.stopPlayingBackgroundMusic();
        this.stopTimer();
        gameWindow.startMultiplayerGame(players, this.currentChannel.getPlayerNickName());
    }

    /**
     * Stop channel checking timer
     */
    private void stopTimer() {
        this.timer.cancel();
    }
}
