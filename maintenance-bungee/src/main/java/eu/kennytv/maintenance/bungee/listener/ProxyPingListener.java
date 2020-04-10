/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2020 KennyTV (https://github.com/KennyTV)
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
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class ProxyPingListener implements Listener {
    private final MaintenanceBungeePlugin plugin;
    private final SettingsProxy settings;

    public ProxyPingListener(final MaintenanceBungeePlugin plugin, final SettingsProxy settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void proxyPing(final ProxyPingEvent event) {
        if (!settings.isMaintenance()) return;

        final ServerPing ping = event.getResponse();
        if (settings.hasCustomPlayerCountMessage()) {
            ping.getVersion().setProtocol(1);
            ping.getVersion().setName(settings.getPlayerCountMessage()
                    .replace("%ONLINE%", Integer.toString(ping.getPlayers().getOnline()))
                    .replace("%MAX%", Integer.toString(ping.getPlayers().getMax())));
        }

        ping.setDescription(settings.getRandomPingMessage());
        ping.getPlayers().setOnline(0);
        ping.getPlayers().setMax(0);
        ping.getPlayers().setSample(new ServerPing.PlayerInfo[]{
                new ServerPing.PlayerInfo(settings.getPlayerCountHoverMessage().replace("%NEWLINE%", "\n"), "")
        });

        if (settings.hasCustomIcon() && plugin.getFavicon() != null)
            ping.setFavicon(plugin.getFavicon());
    }
}
