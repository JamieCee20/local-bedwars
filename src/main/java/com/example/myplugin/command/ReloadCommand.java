package com.example.myplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.example.myplugin.MyPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ReloadCommand implements CommandExecutor {

    private final MyPlugin plugin;

    public ReloadCommand(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("You must be an operator to use this command.", NamedTextColor.RED));
            return true;
        }

        if (plugin.getGameManager().isInGame()) {
            sender.sendMessage(Component.text("Cannot reload config while a game is running.", NamedTextColor.RED));
            return true;
        }

        plugin.reloadConfig();
        plugin.getShopManager().reload(plugin.getConfig());

        // Re-apply team locations if a game world is already loaded
        if (plugin.getGameWorld() != null) {
            plugin.reloadTeamLocations(plugin.getGameWorld());
        }

        sender.sendMessage(Component.text("Config reloaded!", NamedTextColor.GREEN));
        return true;
    }
}
