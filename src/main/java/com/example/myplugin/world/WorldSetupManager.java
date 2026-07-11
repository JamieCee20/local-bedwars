package com.example.myplugin.world;

import java.io.File;
import java.io.FileInputStream;

import com.example.myplugin.game.GameInstance;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

public class WorldSetupManager {

    // Now receives a GameInstance so it knows what world name to use
    // and where to deliver the finished world (instance.setGameWorld).
    private final GameInstance instance;

    public WorldSetupManager(GameInstance instance) {
        this.instance = instance;
    }

    public boolean setupGameWorld() {
        teardownGameWorld();

        // Use the instance's unique world name — e.g. "bedwars_game_1"
        World gameWorld = createVoidWorld(instance.getWorldName());
        if (gameWorld == null) {
            instance.getPlugin().getLogger().severe("Failed to create world '" + instance.getWorldName() + "'.");
            return false;
        }

        gameWorld.setSpawnFlags(false, false);
        configureArenaWorld(gameWorld);

        if (!pasteSchematic(gameWorld)) {
            Bukkit.unloadWorld(gameWorld, false);
            return false;
        }

        // Hand the ready world back to the instance (which also loads team locations)
        instance.setGameWorld(gameWorld);
        return true;
    }

    public void teardownGameWorld() {
        World existing = Bukkit.getWorld(instance.getWorldName());
        if (existing == null) return;

        for (Player player : existing.getPlayers()) {
            player.teleport(instance.getPlugin().getLobbyWorld().getSpawnLocation());
        }

        Bukkit.unloadWorld(existing, false);

        File worldFolder = new File(Bukkit.getWorldContainer(), instance.getWorldName());
        deleteFolder(worldFolder);
    }

    private World createVoidWorld(String name) {
        return new WorldCreator(name)
            .generator(new VoidGenerator())
            .environment(World.Environment.NORMAL)
            .generateStructures(false)
            .createWorld();
    }

    private void configureArenaWorld(World world) {
        world.setStorm(false);
        world.setThundering(false);
        world.setClearWeatherDuration(Integer.MAX_VALUE);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setTime(6000);
    }

    private boolean pasteSchematic(World world) {
        File schematicFile = resolveSchematicFile();
        if (schematicFile == null) return false;

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            instance.getPlugin().getLogger().severe("Unknown schematic format: " + schematicFile.getName());
            return false;
        }

        ConfigurationSection originSec = instance.getPlugin().getConfig().getConfigurationSection("map.origin");
        int ox = originSec != null ? originSec.getInt("x", 0) : 0;
        int oy = originSec != null ? originSec.getInt("y", 64) : 64;
        int oz = originSec != null ? originSec.getInt("z", 0) : 0;

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build()) {
                Operation paste = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(ox, oy, oz))
                    .ignoreAirBlocks(false)
                    .build();
                Operations.complete(paste);
            }
            return true;
        } catch (Exception e) {
            instance.getPlugin().getLogger().severe("Failed to paste schematic: " + e.getMessage());
            return false;
        }
    }

    public File resolveSchematicFile() {
        String name = instance.getPlugin().getConfig().getString("map.schematic", "map.schem");
        File pluginFolder = new File(instance.getPlugin().getDataFolder(), name);
        if (pluginFolder.exists()) return pluginFolder;
        File weFolder = new File(instance.getPlugin().getDataFolder().getParentFile(), "WorldEdit/schematics/" + name);
        if (weFolder.exists()) return weFolder;
        instance.getPlugin().getLogger().severe("Schematic '" + name + "' not found.");
        return null;
    }

    private void deleteFolder(File folder) {
        if (!folder.exists()) return;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) deleteFolder(file);
                else file.delete();
            }
        }
        folder.delete();
    }
}