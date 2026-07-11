package com.example.myplugin.listener;

import com.example.myplugin.game.GameInstance;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameState;
import com.example.myplugin.game.RespawnCountdownTask;
import com.example.myplugin.player.PlayerData;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDeathListener implements Listener {

    private final MyPlugin plugin;

    public PlayerDeathListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // Resolve the instance — null means they died in the hub, ignore
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(player.getUniqueId());
        if (instance == null || instance.getGameManager().getState() != GameState.IN_GAME) return;

        PlayerData data = instance.getPlayerManager().getPlayer(player.getUniqueId());
        if (data == null) return;

        data.setAlive(false);
        boolean bedAlive = instance.getBedManager().isBedAlive(data.getTeam());
        event.getDrops().clear();
        event.setDroppedExp(0);

        if (!bedAlive) player.sendMessage("You have been eliminated!");

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.spigot().respawn(), 1L);
        instance.getGameManager().checkForWinner();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(player.getUniqueId());
        if (instance == null || instance.getGameManager().getState() != GameState.IN_GAME) return;

        PlayerData data = instance.getPlayerManager().getPlayer(player.getUniqueId());
        if (data == null) return;

        Location spawn = instance.getSpawnManager().getSpawn(data.getTeam());
        if (spawn != null) event.setRespawnLocation(spawn);

        boolean bedAlive = instance.getBedManager().isBedAlive(data.getTeam());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (bedAlive) {
                player.setGameMode(GameMode.SPECTATOR);
                // Pass the instance (not plugin) to the countdown task
                new RespawnCountdownTask(instance, player, data, 5).runTaskTimer(plugin, 0L, 20L);
            } else {
                // Bed is gone — enter ghost mode so they can observe but not interfere
                data.setEliminated(true);
                applyGhostMode(player);
            }
        }, 1L);
    }

    // Sets the player up as an invisible, flying observer.
    // ADVENTURE mode means the server's normal block-break rules still apply,
    // and our other listeners provide the remaining restrictions.
    private void applyGhostMode(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setAllowFlight(true);
        player.setFlying(true);
        // Integer.MAX_VALUE ticks ≈ ~3.4 years — effectively permanent for a game session.
        // ambient=false, particles=false, icon=false keeps it completely silent/hidden.
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
    }
}