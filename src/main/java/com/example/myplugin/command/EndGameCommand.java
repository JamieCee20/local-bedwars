package com.example.myplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.util.Messages;

import net.kyori.adventure.text.Component;

public class EndGameCommand implements CommandExecutor {

    private final MyPlugin plugin;

    public EndGameCommand(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!plugin.getGameManager().isInGame()) {
            plugin.getServer().broadcast(Messages.parse("<red>Not currently in an active game!</red>"));
            return false;
        }

        String winner = (args.length > 0) ? args[0] : "Unknown";

        plugin.getServer().broadcast(
                Component.text("Game Over! Winner: " + winner));

        // Delegate to GameManager so reset logic (PlayerManager, BedManager, etc.) is centralised
        plugin.getGameManager().endGame(null);

        return true;
    }
}