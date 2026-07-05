package com.example.myplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.world.VoidGenerator;
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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.FileInputStream;

public class SetupWorldCommand implements CommandExecutor {

    private static final String SETUP_WORLD_NAME = "bedwars_setup";

    private final MyPlugin plugin;

    public SetupWorldCommand(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage(Component.text("You must be an operator to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage:", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("  /bwsetup create  — create void setup world and paste schematic", NamedTextColor.GRAY));
            player.sendMessage(Component.text("  /bwsetup tp      — teleport to the setup world", NamedTextColor.GRAY));
            player.sendMessage(Component.text("  /bwsetup delete  — delete the setup world when done", NamedTextColor.GRAY));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player);
            case "tp"     -> handleTp(player);
            case "delete" -> handleDelete(player);
            default -> player.sendMessage(Component.text("Unknown sub-command. Use /bwsetup for help.", NamedTextColor.RED));
        }

        return true;
    }

    private void handleCreate(Player player) {
        if (Bukkit.getWorld(SETUP_WORLD_NAME) != null) {
            player.sendMessage(Component.text("Setup world already exists! Use /bwsetup tp to go there.", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("Creating void setup world...", NamedTextColor.YELLOW));

        World setupWorld = new WorldCreator(SETUP_WORLD_NAME)
                .generator(new VoidGenerator())
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .createWorld();

        if (setupWorld == null) {
            player.sendMessage(Component.text("Failed to create setup world.", NamedTextColor.RED));
            return;
        }

        File schematicFile = plugin.getWorldSetupManager().resolveSchematicFile();

        if (schematicFile == null) {
            player.sendMessage(Component.text(
                    "Schematic not found — check console for the paths that were searched.", NamedTextColor.RED));
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            player.sendMessage(Component.text("Unknown schematic format: " + schematicFile.getName(), NamedTextColor.RED));
            return;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();

            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(setupWorld);
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld).build()) {

                Operation paste = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(0, 64, 0))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(paste);
            }

            player.teleport(setupWorld.getSpawnLocation());
            player.sendMessage(Component.text("Done! Map pasted at (0, 64, 0).", NamedTextColor.GREEN));
            player.sendMessage(Component.text("Walk to each bed and spawn, press F3, and note the XYZ coordinates.", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Those coordinates are your offsets — copy them into config.yml.", NamedTextColor.YELLOW));
            player.sendMessage(Component.text("Run /bwsetup delete when finished.", NamedTextColor.GRAY));

        } catch (Exception e) {
            player.sendMessage(Component.text("Failed to paste schematic: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Error during setup paste: " + e.getMessage());
        }
    }

    private void handleTp(Player player) {
        World setupWorld = Bukkit.getWorld(SETUP_WORLD_NAME);
        if (setupWorld == null) {
            player.sendMessage(Component.text("Setup world doesn't exist. Run /bwsetup create first.", NamedTextColor.RED));
            return;
        }
        player.teleport(setupWorld.getSpawnLocation());
        player.sendMessage(Component.text("Teleported to setup world.", NamedTextColor.GREEN));
    }

    private void handleDelete(Player player) {
        World setupWorld = Bukkit.getWorld(SETUP_WORLD_NAME);
        if (setupWorld == null) {
            player.sendMessage(Component.text("No setup world to delete.", NamedTextColor.YELLOW));
            return;
        }

        // Move anyone (including the caller) out before unloading
        World lobby = plugin.getLobbyWorld();
        for (Player p : setupWorld.getPlayers()) {
            p.teleport(lobby.getSpawnLocation());
            if (!p.equals(player)) {
                p.sendMessage(Component.text("Setup world deleted — you've been moved to lobby.", NamedTextColor.YELLOW));
            }
        }

        Bukkit.unloadWorld(setupWorld, false);

        File worldFolder = new File(Bukkit.getWorldContainer(), SETUP_WORLD_NAME);
        deleteFolder(worldFolder);

        player.sendMessage(Component.text("Setup world deleted.", NamedTextColor.GREEN));
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
