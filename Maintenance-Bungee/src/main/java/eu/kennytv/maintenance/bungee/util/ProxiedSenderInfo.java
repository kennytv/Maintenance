package eu.kennytv.maintenance.bungee.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public final class ProxiedSenderInfo implements SenderInfo {
    private final CommandSender sender;

    public ProxiedSenderInfo(final CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public UUID getUuid() {
        return sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(final String message) {
        sender.sendMessage(message);
    }

    public void sendMessage(final TextComponent textComponent) {
        sender.sendMessage(textComponent);
    }
}
