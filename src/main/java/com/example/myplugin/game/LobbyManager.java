package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LobbyManager {
    private final MyPlugin plugin;
    private CountdownTask task;

    public LobbyManager(MyPlugin plugin) {
        this.plugin = plugin;
    }

    public void tryStartCountdown() {

        if (task != null)
            return; // already running

        if (plugin.getPlayerManager().getCount() < plugin.getGameManager().getMinPlayers()) {
            return;
        }

        plugin.getServer().broadcast(Component.text("Preparing game world...", NamedTextColor.YELLOW));

        // World creation + schematic pasting runs on the next tick so the command
        // returns immediately, but still executes on the main thread (required by Bukkit).
        plugin.getServer().getScheduler().runTask(plugin, () -> {

            boolean ready = plugin.getWorldSetupManager().setupGameWorld();

            if (!ready) {
                plugin.getServer().broadcast(
                        Component.text("Failed to prepare game world — check console for details.", NamedTextColor.RED));
                return;
            }

            task = new CountdownTask(plugin, plugin.getGameManager().getCountdownTime());
            task.runTaskTimer(plugin, 0L, 20L);

            plugin.getServer().broadcast(Component.text("Countdown Started!", NamedTextColor.GREEN));
        });
    }

    public void cancelCountdownIfNeeded() {

        if (plugin.getPlayerManager().getCount() >= plugin.getGameManager().getMinPlayers()) {
            return;
        }

        if (task != null) {
            task.cancel();
            task = null;
        }

        plugin.getGameManager().setLobby();
        plugin.getWorldSetupManager().teardownGameWorld();
        plugin.getServer().broadcast(Component.text("Countdown cancelled (not enough players)", NamedTextColor.RED));
    }

    public void stopCountdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
