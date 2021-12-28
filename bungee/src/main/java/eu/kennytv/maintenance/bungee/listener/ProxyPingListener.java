/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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
import eu.kennytv.maintenance.bungee.util.ComponentUtil;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class ProxyPingListener implements Listener {
    private final MaintenanceBungeePlugin plugin;
    private final SettingsProxy settings;

    public ProxyPingListener(final MaintenanceBungeePlugin plugin, final SettingsProxy settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = 80)
    public void proxyPing(final ProxyPingEvent event) {
        if (!settings.isMaintenance() || !settings.isEnablePingMessages()) return;

        final ServerPing ping = event.getResponse();
        ServerPing.Players players = ping.getPlayers();
        if (players == null) {
            ping.setPlayers(players = new ServerPing.Players(0, 0,  null));
        }

        if (settings.hasCustomPlayerCountMessage()) {
            ping.setVersion(new ServerPing.Protocol(settings.getPlayerCountMessage()
                    .replace("%ONLINE%", Integer.toString(players.getOnline()))
                    .replace("%MAX%", Integer.toString(players.getMax())), 1));
        }

        ping.setDescriptionComponent(ComponentUtil.toBadComponents(settings.getRandomPingMessage()));

        final String[] split = settings.getPlayerCountHoverMessage().split("\n");
        final ServerPing.PlayerInfo[] samplePlayers = new ServerPing.PlayerInfo[split.length];
        for (int i = 0; i < split.length; i++) {
            samplePlayers[i] = new ServerPing.PlayerInfo(split[i], "");
        }
        players.setSample(samplePlayers);

        if (settings.hasCustomIcon() && plugin.getFavicon() != null) {
            ping.setFavicon(plugin.getFavicon());
        }
    }
}
