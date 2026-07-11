package com.example.myplugin.game;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.example.myplugin.player.PlayerData;
import com.example.myplugin.util.Messages;

import net.kyori.adventure.text.Component;

public class CountdownTask extends BukkitRunnable {

    private final GameInstance instance;
    private int time;

    public CountdownTask(GameInstance instance, int startTime) {
        this.instance = instance;
        this.time = startTime;
    }

    @Override
    public void run() {
        if (instance.getPlayerManager().getCount() < instance.getGameManager().getMinPlayers()) {
            instance.getGameManager().setLobby();
            instance.getPlayerManager().broadcast(Component.text("Not enough players! Aborting countdown."));
            cancel();
            return;
        }

        if (time <= 0) {
            instance.getGameManager().setStarting();
            instance.getLobbyManager().stopCountdown();
            instance.getPlayerManager().broadcast(Messages.parse("<green>Game Starting!!</green>"));

            for (PlayerData data : instance.getPlayerManager().getPlayers()) {
                Player player = instance.getPlugin().getServer().getPlayer(data.getUuid());
                if (player == null) continue;

                player.teleport(instance.getSpawnManager().getSpawn(data.getTeam()));
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                instance.getPlayerManager().freezePlayer(player.getUniqueId());
            }

            new InGameCountdown(instance).runTaskTimer(instance.getPlugin(), 0L, 20L);
            cancel();
            return;
        }

        instance.getPlayerManager().broadcast(
            Messages.parse("<yellow>Game starts in <gold>" + time + "</gold> seconds!</yellow>"));
        time--;
    }
}