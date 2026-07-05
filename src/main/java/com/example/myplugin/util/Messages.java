package com.example.myplugin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Messages {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Messages() {
    }

    public static Component parse(String message) {
        return MM.deserialize(message);
    }
}