package com.example.myplugin.listener;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.game.GameInstance;
import com.example.myplugin.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class GhostListener implements Listener {

    private final MyPlugin plugin;

    public GhostListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    // Prevents eliminated ghosts from dealing damage to active players.
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!isGhost(attacker)) return;
        event.setCancelled(true);
    }

    // Prevents ghosts from using items (right-click, left-click, interacting with
    // chests, buttons, doors, etc.).
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isGhost(event.getPlayer())) return;
        event.setCancelled(true);
    }

    // Prevents ghosts from walking over items and picking them up.
    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isGhost(player)) return;
        event.setCancelled(true);
    }

    // Helper: a player is a ghost if they are in any game and flagged as eliminated.
    private boolean isGhost(Player player) {
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(player.getUniqueId());
        if (instance == null) return false;
        PlayerData data = instance.getPlayerManager().getPlayer(player.getUniqueId());
        return data != null && data.isEliminated();
    }
}
