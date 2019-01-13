package eu.kennytv.maintenance.sponge.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.UUID;

public final class SpongeSenderInfo implements SenderInfo {
    private final CommandSource sender;

    public SpongeSenderInfo(final CommandSource sender) {
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
        sender.sendMessage(Text.of(message));
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }
}
