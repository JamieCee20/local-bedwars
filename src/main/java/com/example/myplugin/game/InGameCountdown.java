package com.example.myplugin.game;

import java.time.Duration;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class InGameCountdown extends BukkitRunnable {

    private final GameInstance instance;
    private int time = 3;

    public InGameCountdown(GameInstance instance) {
        this.instance = instance;
    }

    @Override
    public void run() {
        if (time <= 0) {
            instance.getPlayerManager().unfreezeAll();
            instance.getGameManager().setInGame();

            // Clear any items that spawned during the freeze countdown
            World gameWorld = instance.getGameWorld();
            if (gameWorld != null) {
                for (Entity entity : gameWorld.getEntities()) {
                    if (entity instanceof Item) entity.remove();
                }
            }

            instance.getGeneratorManager().start(instance);
            instance.getGeneratorManager().startCleanupTask(instance);

            // Show "GO!" only to players in this game, not other simultaneous games
            instance.getPlayerManager().showTitle(
                Title.title(
                    Component.text("GO!", NamedTextColor.GREEN),
                    Component.text(""),
                    Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)));
            cancel();
            return;
        }

        instance.getPlayerManager().showTitle(
            Title.title(
                Component.text(String.valueOf(time), NamedTextColor.YELLOW),
                Component.text("Get ready!"),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)));
        time--;
    }
}