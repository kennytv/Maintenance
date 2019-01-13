package eu.kennytv.maintenance.spigot.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public final class BukkitOfflinePlayerInfo implements SenderInfo {
    private final OfflinePlayer player;

    public BukkitOfflinePlayerInfo(final OfflinePlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUuid() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return false;
    }

    @Override
    public void sendMessage(final String message) {
    }

    @Override
    public boolean isPlayer() {
        return true;
    }
}
