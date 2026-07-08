package com.example.myplugin.listener;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameState;
import com.example.myplugin.enums.GameTeam;

import net.kyori.adventure.text.Component;

import java.time.Duration;

public class BlockProtectionListener implements Listener {

    private final MyPlugin plugin;

    public BlockProtectionListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if (plugin.getGameManager().getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        plugin.getPlacedBlocks().add(event.getBlockPlaced());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if (plugin.getGameManager().getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        // BED LOGIC
        GameTeam team = plugin.getBedManager()
                .getTeamFromLocation(event.getBlock().getLocation());

        if (team != null) {

            GameTeam playerTeam = plugin.getPlayerManager().getPlayer(event.getPlayer().getUniqueId()).getTeam();

            if (team == playerTeam) {
                event.setCancelled(true);
                return;
            }

            if (!plugin.getBedManager().isBedAlive(team)) {
                event.setCancelled(true);
                return;
            }

            plugin.getBedManager().breakBed(team);

            plugin.getServer().broadcast(
                    Component.text(team.getDisplayName(), team.getColor()).append(Component.text(" bed has been destroyed!", NamedTextColor.GRAY)));

            // Play ender dragon roar for in-game players only
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (plugin.getPlayerManager().isInGame(p.getUniqueId())) {
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                }
            }

            // If the team has no alive players left, broadcast an elimination title to in-game players
            boolean hasAlivePlayers = plugin.getPlayerManager().getPlayers().stream()
                    .anyMatch(pd -> pd.getTeam() == team && pd.isAlive());

            if (!hasAlivePlayers) {
                Title title = Title.title(
                        Component.text(team.getDisplayName(), team.getColor()),
                        Component.text("has been eliminated!", NamedTextColor.GRAY),
                        Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
                );
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.getPlayerManager().isInGame(p.getUniqueId())) {
                        p.showTitle(title);
                    }
                }
            }

            plugin.getGameManager().checkForWinner();

            event.setDropItems(false);
            return;
        }

        // PLAYER PLACED BLOCKS
        if (plugin.getPlacedBlocks().contains(event.getBlock())) {
            plugin.getPlacedBlocks().remove(event.getBlock());
            event.setCancelled(false);
            return;
        }

        // EVERYTHING ELSE = PROTECTED ARENA
        event.setCancelled(true);
    }
}