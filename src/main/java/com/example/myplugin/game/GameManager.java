package com.example.myplugin.game;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
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
        return plugin.getConfig().getInt("game.min-players", 1);
    }

    public int getCountdownTime() {
        return plugin.getConfig().getInt("game.countdown-time", 10);
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
        Map<GameTeam, Integer> counts = new EnumMap<>(GameTeam.class);
        for (GameTeam team : GameTeam.values()) {
            counts.put(team, 0);
        }

        for (PlayerData data : plugin.getPlayerManager().getPlayers()) {
            if (data.getTeam() != null) {
                counts.merge(data.getTeam(), 1, Integer::sum);
            }
        }

        return counts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(GameTeam.RED);
    }

    public boolean isInGame() {
        return state == GameState.IN_GAME || state == GameState.STARTING;
    }

    public void checkForWinner() {

        Set<GameTeam> participatingTeams = new HashSet<>();
        Set<GameTeam> aliveTeams = new HashSet<>();

        for (PlayerData data : plugin.getPlayerManager().getPlayers()) {
            participatingTeams.add(data.getTeam());
            if (data.isAlive()) {
                aliveTeams.add(data.getTeam());
            }
        }

        boolean devMode = plugin.getConfig().getBoolean("game.dev-mode", false);

        // In normal mode require 2+ teams so a solo tester can't accidentally win immediately.
        // In dev mode this check is skipped so a single player can test the full win flow.
        if (!devMode && participatingTeams.size() < 2) return;

        if (aliveTeams.size() == 1) {
            endGame(aliveTeams.iterator().next());
        } else if (aliveTeams.isEmpty()) {
            endGame(null);
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

        // Only reset players who were actually in the game
        for (PlayerData data : plugin.getPlayerManager().getPlayers()) {
            Player player = plugin.getServer().getPlayer(data.getUuid());
            if (player == null) continue;

            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(plugin.getLobbyWorld().getSpawnLocation());
        }

        // Reset state so a new game can be started fresh
        plugin.getPlayerManager().clearPlayers();
        plugin.getBedManager().resetBeds();

        // Tear down the game world — deleted from disk so the next game gets a clean slate
        plugin.getWorldSetupManager().teardownGameWorld();
    }
}
