package com.example.myplugin.command;

import com.example.myplugin.game.GameInstance;
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
        // If the sender is a player, end their specific game
        if (sender instanceof Player player) {
            GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(player.getUniqueId());
            if (instance == null || !instance.getGameManager().isInGame()) {
                player.sendMessage(Messages.parse("<red>You are not in an active game!</red>"));
                return true;
            }
            instance.getGameManager().endGame(null);
            return true;
        }

        // Console sender — end all active games
        plugin.getInstanceManager().getInstances().forEach(i -> {
            if (i.getGameManager().isInGame()) i.getGameManager().endGame(null);
        });
        sender.sendMessage("All active games ended.");
        return true;
    }
}