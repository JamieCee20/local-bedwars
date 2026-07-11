package com.example.myplugin.game;

import com.example.myplugin.enums.GameState;
import com.example.myplugin.player.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class RespawnCountdownTask extends BukkitRunnable {

    private final GameInstance instance;
    private final Player player;
    private final PlayerData data;
    private int time;

    public RespawnCountdownTask(GameInstance instance, Player player, PlayerData data, int seconds) {
        this.instance = instance;
        this.player = player;
        this.data = data;
        this.time = seconds;
    }

    @Override
    public void run() {
        if (!player.isOnline() || instance.getGameManager().getState() != GameState.IN_GAME) {
            cancel();
            return;
        }

        if (time <= 0) {
            data.setAlive(true);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            player.clearTitle();
            cancel();
            return;
        }

        player.showTitle(Title.title(
            Component.text(String.valueOf(time), NamedTextColor.RED),
            Component.text("Respawning in...", NamedTextColor.GRAY),
            Title.Times.times(Duration.ZERO, Duration.ofMillis(1100), Duration.ZERO)));
        time--;
    }
}