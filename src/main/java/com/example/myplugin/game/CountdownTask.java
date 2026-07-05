package com.example.myplugin.game;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.player.PlayerData;
import com.example.myplugin.util.Messages;

import net.kyori.adventure.text.Component;

public class CountdownTask extends BukkitRunnable {
    private final MyPlugin plugin;
    private int time;

    public CountdownTask(MyPlugin plugin, int startTime) {
        this.plugin = plugin;
        this.time = startTime;
    }

    @Override
    public void run() {
        if (plugin.getPlayerManager().getCount() < plugin.getGameManager().getMinPlayers()) {
            plugin.getGameManager().setLobby();
            plugin.getServer().broadcast(Component.text("Not Enough Players! Aborting Countdown."));
            cancel();
            return;
        }

        if (time <= 0) {
            plugin.getGameManager().setStarting();
            plugin.getLobbyManager().stopCountdown();
            plugin.getServer().broadcast(Messages.parse("<green>Game Starting!!</green>"));

            for (PlayerData data : plugin.getPlayerManager().getPlayers()) {
                Player player = plugin.getServer().getPlayer(data.getUuid());
                if (player == null)
                    continue;

                if (data.getTeam() == GameTeam.RED) {
                    player.teleport(plugin.getSpawnManager().getRedSpawn());
                }

                if (data.getTeam() == GameTeam.BLUE) {
                    player.teleport(plugin.getSpawnManager().getBlueSpawn());
                }

                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
            }
            new InGameCountdown(plugin).runTaskTimer(plugin, 0L, 20L);
            cancel();
            return;
        }

        plugin.getServer().broadcast(
                Messages.parse(
                        "<yellow>Game starts in <gold>" + time + "</gold> seconds!</yellow>"));
        time--;
    }
}
