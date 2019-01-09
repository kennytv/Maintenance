package eu.kennytv.maintenance.spigot.listener;

import eu.kennytv.maintenance.spigot.MaintenanceSpigotBase;
import eu.kennytv.maintenance.spigot.SettingsSpigot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.ServerListPingEvent;

public final class ServerListPingListener extends PingListenerBase {

    public ServerListPingListener(final MaintenanceSpigotBase plugin, final SettingsSpigot settings) {
        super(plugin, settings);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverListPing(final ServerListPingEvent event) {
        if (!settings.isMaintenance()) return;

        event.setMaxPlayers(0);
        event.setMotd(settings.getRandomPingMessage());

        if (settings.hasCustomIcon() && serverIcon != null)
            event.setServerIcon(serverIcon);
    }
}
