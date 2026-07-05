package com.example.myplugin.game;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameState;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.player.PlayerData;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

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

    public boolean isInGame() {
        return state == GameState.IN_GAME || state == GameState.STARTING;
    }

    public void checkForWinner() {

        Set<GameTeam> aliveTeams = new HashSet<>();

        for (PlayerData data : plugin.getPlayerManager().getPlayers()) {

            if (!data.isAlive())
                continue;

            aliveTeams.add(data.getTeam());
        }

        if (aliveTeams.size() == 1) {
            GameTeam winner = aliveTeams.iterator().next();
            endGame(winner);
        }
    }

    public void endGame(GameTeam winner) {

        setLobby();

        plugin.getLobbyManager().stopCountdown();
        plugin.getPlacedBlocks().clear();

        plugin.getServer().showTitle(
                Title.title(
                        Component.text("GAME OVER!", NamedTextColor.GREEN),
                        Component.text(
                                winner != null ? winner.name() + " Team WINS!" : "None",
                                winner != null ? winner.getColor() : null),
                        Title.Times.times(
                                Duration.ofMillis(0),
                                Duration.ofSeconds(2),
                                Duration.ofMillis(0))));
        // plugin.getServer().broadcast(
        // Component.text("Game Over! Winner: " +
        // (winner != null ? winner.name() : "None")));

        for (Player player : plugin.getServer().getOnlinePlayers()) {

            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
        }
    }
}
