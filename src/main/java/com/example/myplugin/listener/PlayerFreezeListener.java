package com.example.myplugin.listener;

import com.example.myplugin.game.GameInstance;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.example.myplugin.MyPlugin;

public class PlayerFreezeListener implements Listener {

    private final MyPlugin plugin;

    public PlayerFreezeListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent event) {
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(event.getPlayer().getUniqueId());
        if (instance == null) return;
        if (!instance.getPlayerManager().isFrozen(event.getPlayer().getUniqueId())) return;
        Location from = event.getFrom();
        Location to = event.getTo();

        // Allow head rotation (yaw/pitch) but block any position change
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            // Preserve the player's current look direction so it doesn't feel jarring
            Location frozen = from.clone();
            frozen.setYaw(to.getYaw());
            frozen.setPitch(to.getPitch());
            event.setTo(frozen);
        }
    }
}
