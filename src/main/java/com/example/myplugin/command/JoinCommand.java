package com.example.myplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.player.PlayerData;

import net.kyori.adventure.text.Component;

public class JoinCommand implements CommandExecutor {

    private final MyPlugin plugin;

    public JoinCommand(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (!plugin.getGameManager().isLobby()) {
            player.sendMessage("Game already running!");
            return true;
        }

        if (plugin.getPlayerManager().isInGame(player.getUniqueId())) {
            player.sendMessage("You are already in the game!");
            return true;
        }

        PlayerData data = plugin.getPlayerManager()
                .addPlayer(player.getUniqueId());

        GameTeam team = plugin.getGameManager()
                .getNextTeam();

        data.setTeam(team);

        player.sendMessage(
                Component.text("You joined ")
                        .append(team.getDisplayComponent())
                        .append(
                                Component.text(
                                        " team! Players: " + plugin.getPlayerManager().getCount())));

        plugin.getLobbyManager().tryStartCountdown();

        return true;
    }
}