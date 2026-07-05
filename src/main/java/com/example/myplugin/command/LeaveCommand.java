package com.example.myplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.util.Messages;

public class LeaveCommand implements CommandExecutor {
    private final MyPlugin plugin;

    public LeaveCommand(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player))
            return true;

        if (!plugin.getPlayerManager().isInGame(player.getUniqueId())) {
            player.sendMessage("You're not in the game!");
            return true;
        }

        plugin.getPlayerManager().removePlayer(player.getUniqueId());

        player.sendMessage(Messages.parse(
                "<red>You have left the game!</red>"));

        plugin.getLobbyManager().cancelCountdownIfNeeded();

        return true;
    }

}
