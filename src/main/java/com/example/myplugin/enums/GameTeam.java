package com.example.myplugin.enums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public enum GameTeam {
    RED(NamedTextColor.RED, "Red"),
    BLUE(NamedTextColor.BLUE, "Blue"),
    GREEN(NamedTextColor.GREEN, "Green"),
    YELLOW(NamedTextColor.YELLOW, "Yellow");

    private final NamedTextColor color;
    private final String displayName;

    GameTeam(NamedTextColor color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

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
