package com.example.myplugin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.GameState;

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

        // HARD BLOCK EVERYTHING FIRST
        if (plugin.getGameManager().getState() != GameState.IN_GAME) {
            event.setCancelled(true);
            return;
        }

        // default rule: DO NOT allow breaking anything
        event.setCancelled(true);

        // only allow player placed blocks
        if (plugin.getPlacedBlocks().contains(event.getBlock())) {
            plugin.getPlacedBlocks().remove(event.getBlock());
            event.setCancelled(false);
        }
    }
}