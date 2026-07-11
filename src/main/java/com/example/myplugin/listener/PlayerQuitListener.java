package com.example.myplugin.listener;

import com.example.myplugin.game.GameInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.example.myplugin.MyPlugin;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final MyPlugin plugin;

    public PlayerQuitListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // Find which game they were in — null means they were just in the hub
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(uuid);
        if (instance == null) return;

        boolean wasInGame = instance.getGameManager().isInGame();
        instance.getPlayerManager().removePlayer(uuid);
        event.getPlayer().getInventory().clear();

        if (wasInGame) instance.getGameManager().checkForWinner();
        else instance.getLobbyManager().cancelCountdownIfNeeded();
    }
}
