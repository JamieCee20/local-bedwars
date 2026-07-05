package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;

import net.kyori.adventure.text.Component;

public class LobbyManager {
    private final MyPlugin plugin;
    private CountdownTask task;
    private boolean running = false;

    public LobbyManager(MyPlugin plugin) {
        this.plugin = plugin;
    }

    public void tryStartCountdown() {

        if (task != null)
            return; // already running

        if (plugin.getPlayerManager().getCount() < plugin.getGameManager().getMinPlayers()) {
            return;
        }

        task = new CountdownTask(plugin, plugin.getGameManager().getCountdownTime());
        task.runTaskTimer(plugin, 0L, 20L);

        plugin.getServer().broadcast(Component.text("Countdown Started!"));
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
        plugin.getServer().broadcast(Component.text("Countdown cancelled (not enough players)"));
    }

    public void stopCountdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        running = false;
    }
}
