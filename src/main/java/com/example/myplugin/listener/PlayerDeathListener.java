package com.example.myplugin.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameState;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.player.PlayerData;

public class PlayerDeathListener implements Listener {

    private final MyPlugin plugin;

    public PlayerDeathListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        if (plugin.getGameManager().getState() != GameState.IN_GAME) {
            return;
        }

        Player player = event.getEntity();

        PlayerData data = plugin.getPlayerManager()
                .getPlayer(player.getUniqueId());

        if (data == null)
            return;

        data.setAlive(false);

        GameTeam team = data.getTeam();

        boolean bedAlive = plugin.getBedManager().isBedAlive(team);

        // clear drops / XP (important for BedWars)
        event.getDrops().clear();
        event.setDroppedExp(0);

        if (bedAlive) {
            handleRespawn(player, data, team);
        } else {
            handleElimination(player, data);
        }

        plugin.getGameManager().checkForWinner();
    }

    private void handleRespawn(Player player, PlayerData data, GameTeam team) {

        player.sendMessage("You will respawn in 5 seconds...");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            if (plugin.getGameManager().getState() != GameState.IN_GAME)
                return;

            data.setAlive(true);

            player.teleport(getTeamSpawn(team));
            player.setHealth(20);
            player.setFoodLevel(20);
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);

            player.sendMessage("You have respawned!");

        }, 20L * 5);
    }

    private Location getTeamSpawn(GameTeam team) {
        return plugin.getSpawnManager().getSpawn(team);
    }

    private void handleElimination(Player player, PlayerData data) {

        player.sendMessage("You have been eliminated!");

        player.setGameMode(GameMode.SPECTATOR);
        data.setAlive(false);
    }
}