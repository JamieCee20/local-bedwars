package com.example.myplugin.game;

import com.example.myplugin.MyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class LobbyManager {

    private final GameInstance instance;
    private CountdownTask task;
    private boolean settingUp = false;

    public LobbyManager(GameInstance instance) {
        this.instance = instance;
    }

    public void tryStartCountdown() {
        if (task != null || settingUp) return;

        if (instance.getPlayerManager().getCount() < instance.getGameManager().getMinPlayers()) return;

        settingUp = true;
        instance.getPlayerManager().broadcast(Component.text("Preparing game world...", NamedTextColor.YELLOW));

        // World setup must run on the main thread but we defer it one tick so the
        // command/join call that triggered this returns cleanly first.
        MyPlugin plugin = instance.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, () -> {

            boolean ready = instance.getWorldSetupManager().setupGameWorld();
            settingUp = false;

            if (!ready) {
                instance.getPlayerManager().broadcast(
                    Component.text("Failed to prepare game world — check console.", NamedTextColor.RED));
                return;
            }

            task = new CountdownTask(instance, instance.getGameManager().getCountdownTime());
            task.runTaskTimer(plugin, 0L, 20L);

            instance.getPlayerManager().broadcast(Component.text("Countdown Started!", NamedTextColor.GREEN));
        });
    }

    public void cancelCountdownIfNeeded() {
        if (instance.getPlayerManager().getCount() >= instance.getGameManager().getMinPlayers()) return;

        settingUp = false;
        if (task != null) {
            task.cancel();
            task = null;
        }

        instance.getGameManager().setLobby();
        instance.getShopManager().despawnShopEntity();
        instance.getWorldSetupManager().teardownGameWorld();

        int current = instance.getPlayerManager().getCount();
        int min = instance.getGameManager().getMinPlayers();
        instance.getPlayerManager().broadcast(
            Component.text("Countdown cancelled — waiting for " + (min - current) + " more player(s).", NamedTextColor.RED));
    }

    public void stopCountdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}