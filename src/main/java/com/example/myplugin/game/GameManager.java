package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameState;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.player.PlayerData;

public class GameManager {
    private GameState state = GameState.LOBBY;

    private final int MIN_PLAYERS = 1;
    private final int COUNTDOWN_TIME = 10;

    private final MyPlugin plugin;

    public GameManager(MyPlugin plugin) {
        this.plugin = plugin;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public boolean isLobby() {
        return state == GameState.LOBBY;
    }

    public int getMinPlayers() {
        return MIN_PLAYERS;
    }

    public int getCountdownTime() {
        return COUNTDOWN_TIME;
    }

    public void setLobby() {
        state = GameState.LOBBY;
    }

    public void setStarting() {
        state = GameState.STARTING;
    }

    public void setInGame() {
        state = GameState.IN_GAME;
    }

    public GameTeam getNextTeam() {
        int red = 0;
        int blue = 0;

        for (PlayerData data : plugin.getPlayerManager().getPlayers()) {

            if (data.getTeam() == GameTeam.RED) {
                red++;
            }

            if (data.getTeam() == GameTeam.BLUE) {
                blue++;
            }
        }

        return red <= blue
                ? GameTeam.RED
                : GameTeam.BLUE;
    }
}
