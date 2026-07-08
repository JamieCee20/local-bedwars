package com.example.myplugin.enums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public enum GameTeam {
    RED(NamedTextColor.RED,    "Red",    Material.RED_WOOL, Material.RED_TERRACOTTA),
    BLUE(NamedTextColor.BLUE,  "Blue",   Material.BLUE_WOOL, Material.BLUE_TERRACOTTA),
    GREEN(NamedTextColor.GREEN, "Green", Material.GREEN_WOOL, Material.GREEN_TERRACOTTA),
    YELLOW(NamedTextColor.YELLOW, "Yellow", Material.YELLOW_WOOL, Material.YELLOW_TERRACOTTA);

    private final NamedTextColor color;
    private final String displayName;
    private final Material woolMaterial;
    private final Material terracottaMaterial;

    GameTeam(NamedTextColor color, String displayName, Material woolMaterial, Material terracottaMaterial) {
        this.color = color;
        this.displayName = displayName;
        this.woolMaterial = woolMaterial;
        this.terracottaMaterial = terracottaMaterial;
    }

    public Material getWoolMaterial() {
        return woolMaterial;
    }

    public Material getTerracottaMaterial() {return terracottaMaterial;}

    public NamedTextColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Component getDisplayComponent() {
        return Component.text(displayName, color);
    }
}
