package eu.kennytv.maintenance.core.listener;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class JoinListenerBase {
    protected final MaintenanceModePlugin plugin;
    protected final Settings settings;
    private final Set<UUID> notifiedPlayers = new HashSet<>();
    private final UUID notifyUuid = new UUID(-6334418481592579467L, -4779835342378829761L);

    protected JoinListenerBase(final MaintenanceModePlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    /**
     * Deals with the login and returns true if the player should be kicked after this method.
     *
     * @param sender wrapper of the joining player
     * @return true if the sender should be kicked
     */
    public boolean handleLogin(final SenderInfo sender) {
        if (sender.getUuid().equals(notifyUuid))
            sender.sendMessage("§6MaintenanceBungee §aVersion " + plugin.getVersion());
        else if (settings.isMaintenance()) {
            if (!sender.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(sender.getUuid())) {
                if (settings.isJoinNotifications())
                    broadcastJoinNotification(sender);
                return true;
            }
        }

        if (!sender.hasPermission("maintenance.admin") || notifiedPlayers.contains(sender.getUuid())) return false;

        plugin.async(() -> {
            if (!plugin.updateAvailable()) return;
            notifiedPlayers.add(sender.getUuid());
            plugin.sendUpdateNotification(sender);
        });
        return false;
    }

    protected abstract void broadcastJoinNotification(SenderInfo sender);
}
