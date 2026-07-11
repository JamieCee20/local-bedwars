package com.example.myplugin.command;

import com.example.myplugin.game.GameInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameTeam;
import com.example.myplugin.player.PlayerData;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JoinCommand implements CommandExecutor {

    private final MyPlugin plugin;

    public JoinCommand(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        // Make sure the player isn't already in a game
        if (plugin.getInstanceManager().getInstanceForPlayer(player.getUniqueId()) != null) {
            player.sendMessage("You are already in a game!");
            return true;
        }

        // Find a waiting game or spin up a new one
        GameInstance instance = plugin.getInstanceManager().getOrCreateAvailableInstance();

        if (!instance.getGameManager().isLobby()) {
            player.sendMessage("No available games right now, try again shortly!");
            return true;
        }

        PlayerData data = instance.getPlayerManager().addPlayer(player.getUniqueId());
        GameTeam team = instance.getGameManager().getNextTeam();
        data.setTeam(team);

        int current = instance.getPlayerManager().getCount();
        int min = instance.getGameManager().getMinPlayers();

        instance.getPlayerManager().broadcast(
            Component.text(player.getName() + " joined ", NamedTextColor.YELLOW)
                .append(team.getDisplayComponent())
                .append(Component.text(" team! ", NamedTextColor.YELLOW))
                .append(Component.text("(" + current + "/" + min + " players)", NamedTextColor.GRAY)));

        if (current < min) {
            instance.getPlayerManager().broadcast(
                Component.text("Waiting for " + (min - current) + " more player(s)...", NamedTextColor.GRAY));
        }

        instance.getLobbyManager().tryStartCountdown();
        return true;
    }
}