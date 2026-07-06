package com.example.myplugin.game;

import java.time.Duration;

import org.bukkit.scheduler.BukkitRunnable;

import com.example.myplugin.MyPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class InGameCountdown extends BukkitRunnable {

    private final MyPlugin plugin;
    private int time = 3;

    public InGameCountdown(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        if (time <= 0) {

            plugin.getPlayerManager().unfreezeAll();
            plugin.getGameManager().setInGame();
            plugin.getGeneratorManager().start(plugin);
            plugin.getGeneratorManager().startCleanupTask(plugin);

            plugin.getServer().showTitle(
                Title.title(
                    Component.text("GO!", NamedTextColor.GREEN),
                    Component.text(""),
                    Title.Times.times(
                        Duration.ofMillis(0),
                        Duration.ofSeconds(1),
                        Duration.ofMillis(0))));

            cancel();
            return;
        }

        plugin.getServer().showTitle(
            Title.title(
                Component.text(String.valueOf(time), NamedTextColor.YELLOW),
                Component.text("Get ready!"),
                Title.Times.times(
                    Duration.ofMillis(0),
                    Duration.ofSeconds(1),
                    Duration.ofMillis(0))));

        time--;
    }
}