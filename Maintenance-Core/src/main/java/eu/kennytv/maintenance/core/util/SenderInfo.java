package eu.kennytv.maintenance.core.util;

import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public abstract class SenderInfo {

    public abstract UUID getUuid();

    public abstract String getName();

    public abstract boolean hasPermission(String permission);

    public abstract void sendMessage(String message);

    public abstract void sendMessage(TextComponent textComponent);
}