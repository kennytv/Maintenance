package eu.kennytv.maintenance.spigot.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class BukkitSenderInfo implements SenderInfo {
    private final CommandSender sender;

    public BukkitSenderInfo(final CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public UUID getUuid() {
        return sender instanceof Player ? ((Player) sender).getUniqueId() : null;
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
}
