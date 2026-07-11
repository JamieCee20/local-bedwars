package com.example.myplugin.listener;

import com.example.myplugin.game.GameInstance;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
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
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(event.getPlayer().getUniqueId());
        // Not in a Bedwars game — not our concern, let other plugins or vanilla decide
        if (instance == null) return;
        if (instance.getGameManager().getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }
        // Ghost players (eliminated, bed gone) cannot place blocks
        var data = instance.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (data != null && data.isEliminated()) {
            event.setCancelled(true);
            return;
        }
        instance.getPlacedBlocks().add(event.getBlockPlaced());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(event.getPlayer().getUniqueId());
        // Not in a Bedwars game — not our concern
        if (instance == null) return;
        if (instance.getGameManager().getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }
        // Ghost players cannot break blocks either
        var data = instance.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (data != null && data.isEliminated()) {
            event.setCancelled(true);
            return;
        }

        // BED LOGIC — use instance managers throughout
        GameTeam team = instance.getBedManager().getTeamFromLocation(event.getBlock().getLocation());
        if (team != null) {
            GameTeam playerTeam = instance.getPlayerManager().getPlayer(event.getPlayer().getUniqueId()).getTeam();
            if (team == playerTeam) { event.setCancelled(true); return; }
            if (!instance.getBedManager().isBedAlive(team)) { event.setCancelled(true); return; }

            // Beds are two blocks. We remove both halves manually and cancel the event.
            // If we let vanilla handle it, Paper fires a second BlockBreakEvent for the
            // companion half — which our isBedAlive() check then cancels, leaving a ghost block.
            Block broken = event.getBlock();
            Block companion = null;
            if (broken.getBlockData() instanceof Bed bedData) {
                companion = bedData.getPart() == Bed.Part.FOOT
                    ? broken.getRelative(bedData.getFacing())
                    : broken.getRelative(bedData.getFacing().getOppositeFace());
            }
            broken.setType(Material.AIR);
            if (companion != null) companion.setType(Material.AIR);
            event.setCancelled(true); // prevent vanilla drop/xp since we removed the block manually

            instance.getBedManager().breakBed(team);

            instance.getPlayerManager().broadcast(
                Component.text(team.getDisplayName(), team.getColor())
                    .append(Component.text(" bed has been destroyed!", NamedTextColor.GRAY)));

            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (instance.getPlayerManager().isInGame(p.getUniqueId())) {
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                }
            }

            boolean hasAlivePlayers = instance.getPlayerManager().getPlayers().stream()
                .anyMatch(pd -> pd.getTeam() == team && pd.isAlive());
            if (!hasAlivePlayers) {
                Title title = Title.title(
                    Component.text(team.getDisplayName(), team.getColor()),
                    Component.text("has been eliminated!", NamedTextColor.GRAY),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500)));
                instance.getPlayerManager().showTitle(title);
            }

            instance.getGameManager().checkForWinner();
            return;
        }

        if (instance.getPlacedBlocks().contains(event.getBlock())) {
            instance.getPlacedBlocks().remove(event.getBlock());
            event.setCancelled(false);
            return;
        }

        event.setCancelled(true);
    }
}