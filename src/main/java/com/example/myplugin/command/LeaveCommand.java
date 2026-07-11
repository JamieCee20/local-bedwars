package com.example.myplugin.command;

import com.example.myplugin.game.GameInstance;
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
        if (!(sender instanceof Player player)) return true;

        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(player.getUniqueId());
        if (instance == null) {
            player.sendMessage("You're not in a game!");
            return true;
        }

        boolean wasInGame = instance.getGameManager().isInGame();
        instance.getPlayerManager().removePlayer(player.getUniqueId());
        instance.getPlayerManager().unfreezeAll();

        player.getInventory().clear();
        player.sendMessage(Messages.parse("<red>You have left the game!</red>"));
        player.teleport(plugin.getLobbyWorld().getSpawnLocation());

        if (wasInGame) instance.getGameManager().checkForWinner();
        else instance.getLobbyManager().cancelCountdownIfNeeded();

        return true;
    }

}
