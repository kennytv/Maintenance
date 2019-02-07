/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.kennytv.maintenance.bungee.listener;

import eu.kennytv.maintenance.bungee.MaintenanceBungeePlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class ServerConnectListener implements Listener {
    private final MaintenanceBungeePlugin plugin;
    private final SettingsProxy settings;
    private boolean warned;

    public ServerConnectListener(final MaintenanceBungeePlugin plugin, final SettingsProxy settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverConnect(final ServerConnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        final ServerInfo target = event.getTarget();
        if (!plugin.isMaintenance(target)) return;
        if (plugin.hasPermission(player, "maintenance.bypass") || settings.getWhitelistedPlayers().containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        if (settings.isJoinNotifications()) {
            final BaseComponent[] s = TextComponent.fromLegacyText(settings.getMessage("joinNotification").replace("%PLAYER%", player.getName()));
            target.getPlayers().stream().filter(p -> plugin.hasPermission(p, "joinnotification")).forEach(p -> p.sendMessage(s));
        }
        // Normal serverconnect
        if (event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY && event.getReason() != ServerConnectEvent.Reason.KICK_REDIRECT
                && event.getReason() != ServerConnectEvent.Reason.LOBBY_FALLBACK && event.getReason() != ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT) {
            player.sendMessage(settings.getMessage("singleMaintenanceKick").replace("%SERVER%", target.getName()));
            return;
        }

        // If it's the initial proxy join or a kick from another server, go back to fallback server
        final ServerInfo fallback = plugin.getProxy().getServerInfo(settings.getFallbackServer());
        if (fallback == null || !fallback.canAccess(player) || plugin.isMaintenance(fallback)) {
            player.disconnect(settings.getMessage("singleMaintenanceKickComplete").replace("%NEWLINE%", "\n").replace("%SERVER%", target.getName()));
            if (!warned) {
                plugin.getLogger().warning("Could not send player to the fallback server set in the SpigotServers.yml! Instead kicking player off the network!");
                warned = true;
            }
        } else {
            event.setTarget(fallback);
        }
    }
}
