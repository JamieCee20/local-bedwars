package com.example.myplugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.example.myplugin.MyPlugin;

public class PlayerJoinListener implements Listener {

    private final MyPlugin plugin;

    public PlayerJoinListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().teleport(plugin.getLobbyWorld().getSpawnLocation());
    }
}
