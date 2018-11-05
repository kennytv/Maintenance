package eu.kennytv.maintenance.bungee.listener;

import eu.kennytv.maintenance.bungee.SettingsBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class ServerConnectListener implements Listener {
    private final SettingsBungee settings;

    public ServerConnectListener(final SettingsBungee settings) {
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverConnect(final ServerConnectEvent event) {
        final ProxiedPlayer p = event.getPlayer();
        final ServerInfo target = event.getTarget();
        if (settings.getMaintenanceServers().contains(target.getName())) {
            if (!p.hasPermission("maintenance.bypass") && !settings.getWhitelistedPlayers().containsKey(p.getUniqueId())) {
                event.setCancelled(true);
                //TODO message
                p.sendMessage("");
                if (settings.isJoinNotifications())
                    target.getPlayers().stream().filter(player -> player.hasPermission("maintenance.joinnotification"))
                            .forEach(player -> player.sendMessage(settings.getMessage("joinNotification").replace("%PLAYER%", p.getName())));
            }
        }
    }
}
