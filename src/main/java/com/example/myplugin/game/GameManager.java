package com.example.myplugin.game;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
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

        // Track which teams have at least one alive player
        Map<GameTeam, Boolean> teamHasAlivePlayer = new EnumMap<>(GameTeam.class);
        for (PlayerData data : plugin.getPlayerManager().getPlayers()) {
            GameTeam team = data.getTeam();
            if (data.isAlive()) {
                teamHasAlivePlayer.put(team, true);
            } else {
                teamHasAlivePlayer.putIfAbsent(team, false);
            }
        }

        // A team is still "in the game" if it has a living bed OR a living player.
        // This ensures teams with beds but no players (e.g. in dev-mode solo tests)
        // still count as active and must be eliminated before a winner is declared.
        BedManager bedManager = plugin.getBedManager();
        Set<GameTeam> teamsInGame = new HashSet<>();
        for (GameTeam team : bedManager.getAllTeams()) {
            boolean hasBed = bedManager.isBedAlive(team);
            boolean hasAlivePlayer = teamHasAlivePlayer.getOrDefault(team, false);
            if (hasBed || hasAlivePlayer) {
                teamsInGame.add(team);
            }
        }

        boolean devMode = plugin.getConfig().getBoolean("game.dev-mode", false);

        // In normal mode require 2+ teams so a solo tester can't accidentally win immediately.
        // In dev mode this check is skipped so a single player can test the full win flow.
        if (!devMode && teamsInGame.size() < 2) return;

        if(plugin.getPlayerManager().getPlayers().isEmpty()) {
            plugin.getLogger().info("Game ended due to no players");
            endGame(null);
            return;
        }

        if (teamsInGame.size() == 1) {
            endGame(teamsInGame.iterator().next());
        } else if (teamsInGame.isEmpty()) {
            endGame(null);
        }
    }

    public void endGame(GameTeam winner) {

        setLobby();

        plugin.getLobbyManager().stopCountdown();
        plugin.getGeneratorManager().stop();
        plugin.getPlacedBlocks().clear();

        plugin.getServer().showTitle(
                Title.title(
                        Component.text("GAME OVER!", NamedTextColor.GREEN),
                        Component.text(
                                winner != null ? winner.name() + " Team WINS!" : "None",
                                winner != null ? winner.getColor() : null),
                        Title.Times.times(
                                Duration.ofMillis(0),
                                Duration.ofSeconds(4),
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

        // Clear all item drops from the game world before tearing it down
        World gameWorld = plugin.getGameWorld();
        if (gameWorld != null) {
            for (Entity entity : gameWorld.getEntities()) {
                if (entity instanceof Item) {
                    entity.remove();
                }
            }
        }

        plugin.getShopManager().despawnShopEntity();

        // Tear down the game world — deleted from disk so the next game gets a clean slate
        plugin.getWorldSetupManager().teardownGameWorld();
    }
}
