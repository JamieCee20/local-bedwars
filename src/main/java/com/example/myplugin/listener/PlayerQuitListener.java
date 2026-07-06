package com.example.myplugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.example.myplugin.MyPlugin;

public class PlayerQuitListener implements Listener {

    private final MyPlugin plugin;

    public PlayerQuitListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();

        if (!plugin.getPlayerManager().isInGame(uuid)) return;

        boolean wasInGame = plugin.getGameManager().isInGame();

        plugin.getPlayerManager().removePlayer(uuid);
        event.getPlayer().getInventory().clear();

        if (wasInGame) {
            plugin.getGameManager().checkForWinner();
        } else {
            plugin.getLobbyManager().cancelCountdownIfNeeded();
        }
    }
}
