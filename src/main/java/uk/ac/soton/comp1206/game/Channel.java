package uk.ac.soton.comp1206.game;

import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.HashSet;

/**
 * Custom channel class to hold core logic in the current channel joined to by the user
 */
public class Channel {
    /**
     * Channel name
     */
    private String name;
    /**
     * Players in channel
     */
    private SimpleSetProperty<String> players = new SimpleSetProperty<String>();
    /**
     * Current player nickname
     */
    private SimpleStringProperty playerNickName = new SimpleStringProperty();
    /**
     * Is the current player host
     */
    private boolean isHost;
    /**
     * Is the channel parted from
     */
    private boolean parted;

    /**
     * Create a new channel with just a name
     * @param name channel name
     */
    public Channel(String name) {
        this.name = name;

        initialise();
    }

    /**
     * Create a channel with a name and set if host
     * @param name channel name
     * @param isHost is the player the host
     */
    public Channel(String name, boolean isHost) {
        this.name = name;
        this.isHost = isHost;

        initialise();
    }

    /**
     * Reset channel/Blank channel
     */
    public Channel() {
        this.name = "";

        initialise();
    }

    /**
     * Initialise channel
     */
    private void initialise() {
        //Set players property with observable hashset
        this.players.set(FXCollections.observableSet(new HashSet<String>()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObservableSet<String> getPlayers() {
        return players.get();
    }

    public void setPlayers(HashSet<String> players) {
        this.players.clear();
        this.players.addAll(players);
    }

    public void addPlayer(String player) {
        this.players.add(player);
    }

    public void removePlayer(String player) {
        this.players.remove(player);
    }

    public String getPlayerNickName() {
        return playerNickName.get();
    }

    public StringProperty playerNicknameProperty() {
        return this.playerNickName;
    }

    public void setPlayerNickName(String playerNickName) {
        this.playerNickName.set(playerNickName);
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public boolean isParted() {
        return parted;
    }

    public void setParted(boolean parted) {
        this.parted = parted;
    }

    public boolean playerExists(String player) {
        return this.players.contains(player);
    }

    /**
     * Updates a player name
     * @param oldName old name
     * @param newName new name
     */
    public void updatePlayerName(String oldName, String newName) {
        this.players.remove(oldName);
        this.players.add(newName);
        if(oldName.equals(this.playerNickName)) {
            this.setPlayerNickName(newName);
        }
    }

    /**
     * Resets the current channel
     */
    public void leaveChannel() {
        this.parted = true;
        this.name = "";
        this.isHost = false;
    }

    /**
     * Sets object up for joining a new channel
     * @param name new channel name
     */
    public void joinChannel(String name) {
        this.parted = false;
        this.name = name;
        this.isHost = false;
    }

    public SetProperty playersProperty() {
        return this.players;
    }
}
