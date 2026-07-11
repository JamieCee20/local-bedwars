package com.example.myplugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.example.myplugin.MyPlugin;

public class PlayerJoinListener implements Listener {

    public PlayerJoinListener(MyPlugin plugin) {
        // Reserved for any future Bedwars-specific join handling
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Bedwars has no opinion on where a player spawns when joining the server.
        // Spawn/hub routing belongs to a dedicated hub plugin.
    }
}
