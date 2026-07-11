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

import com.example.myplugin.enums.GameState;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.player.PlayerData;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class GameManager {

    private GameState state = GameState.LOBBY;

    // GameManager now holds a reference to its GameInstance rather than the plugin.
    // This means it can only affect its own game's state — other games are untouched.
    private final GameInstance instance;

    public GameManager(GameInstance instance) {
        this.instance = instance;
    }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public boolean isLobby()  { return state == GameState.LOBBY; }
    public boolean isInGame() { return state == GameState.IN_GAME || state == GameState.STARTING; }
    public void setLobby()    { state = GameState.LOBBY; }
    public void setStarting() { state = GameState.STARTING; }
    public void setInGame()   { state = GameState.IN_GAME; }

    // Config is global so we go via getPlugin() for these
    public int getMinPlayers()    { return instance.getPlugin().getConfig().getInt("game.min-players", 2); }
    public int getCountdownTime() { return instance.getPlugin().getConfig().getInt("game.countdown-time", 10); }

    public GameTeam getNextTeam() {
        Map<GameTeam, Integer> counts = new EnumMap<>(GameTeam.class);
        for (GameTeam team : GameTeam.values()) counts.put(team, 0);
        for (PlayerData data : instance.getPlayerManager().getPlayers()) {
            if (data.getTeam() != null) counts.merge(data.getTeam(), 1, Integer::sum);
        }
        return counts.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(GameTeam.RED);
    }

    public void checkForWinner() {
        Map<GameTeam, Boolean> teamHasAlivePlayer = new EnumMap<>(GameTeam.class);
        for (PlayerData data : instance.getPlayerManager().getPlayers()) {
            GameTeam team = data.getTeam();
            if (data.isAlive()) teamHasAlivePlayer.put(team, true);
            else teamHasAlivePlayer.putIfAbsent(team, false);
        }

        BedManager bedManager = instance.getBedManager();
        Set<GameTeam> teamsInGame = new HashSet<>();
        for (GameTeam team : bedManager.getAllTeams()) {
            if (bedManager.isBedAlive(team) || teamHasAlivePlayer.getOrDefault(team, false)) {
                teamsInGame.add(team);
            }
        }

        boolean devMode = instance.getPlugin().getConfig().getBoolean("game.dev-mode", false);
        if (!devMode && teamsInGame.size() < 2) return;

        if (instance.getPlayerManager().getPlayers().isEmpty()) {
            endGame(null);
            return;
        }

        if (teamsInGame.size() == 1) endGame(teamsInGame.iterator().next());
        else if (teamsInGame.isEmpty()) endGame(null);
    }

    public void endGame(GameTeam winner) {
        setLobby();

        instance.getLobbyManager().stopCountdown();
        instance.getGeneratorManager().stop();
        instance.getPlacedBlocks().clear();

        // Show the game-over title only to players who were in this game,
        // not to players in other simultaneous games.
        instance.getPlayerManager().showTitle(
            Title.title(
                Component.text("GAME OVER!", NamedTextColor.GREEN),
                Component.text(
                    winner != null ? winner.name() + " Team WINS!" : "None",
                    winner != null ? winner.getColor() : NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(4), Duration.ZERO)));

        for (PlayerData data : instance.getPlayerManager().getPlayers()) {
            Player player = instance.getPlugin().getServer().getPlayer(data.getUuid());
            if (player == null) continue;
            player.getInventory().clear();
            // Strip ghost effects if this player was eliminated mid-game
            if (data.isEliminated()) {
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            player.setGameMode(GameMode.ADVENTURE);
            // Send them back to the hub world
            player.teleport(instance.getPlugin().getLobbyWorld().getSpawnLocation());
        }

        instance.getPlayerManager().clearPlayers();
        instance.getBedManager().resetBeds();

        // Clean up item drops in the game world before tearing it down
        World gameWorld = instance.getGameWorld();
        if (gameWorld != null) {
            for (Entity entity : gameWorld.getEntities()) {
                if (entity instanceof Item) entity.remove();
            }
        }

        instance.getShopManager().despawnShopEntity();
        instance.getWorldSetupManager().teardownGameWorld();

        // Remove this game from the live list so the InstanceManager stops routing
        // players or events to it. It will be garbage-collected once no references remain.
        instance.getPlugin().getInstanceManager().removeInstance(instance);
    }
}