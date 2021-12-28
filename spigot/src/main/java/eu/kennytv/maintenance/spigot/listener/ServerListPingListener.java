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
package eu.kennytv.maintenance.spigot.listener;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import eu.kennytv.maintenance.spigot.util.ComponentUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public final class ServerListPingListener implements Listener {
    private final MaintenanceSpigotPlugin plugin;
    private final Settings settings;

    public ServerListPingListener(final MaintenanceSpigotPlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverListPing(final ServerListPingEvent event) {
        if (!settings.isMaintenance() || !settings.isEnablePingMessages()) return;

        if (settings.hasCustomPlayerCountMessage()) {
            event.setMaxPlayers(0);
        }

        if (ComponentUtil.PAPER) {
            event.motd(ComponentUtil.toPaperComponent(settings.getRandomPingMessage()));
        } else {
            event.setMotd(ComponentUtil.toLegacy(settings.getRandomPingMessage()));
        }

        if (settings.hasCustomIcon() && plugin.getFavicon() != null) {
            try {
                event.setServerIcon(plugin.getFavicon());
            } catch (final UnsupportedOperationException ignored) {
                // Thrown in a ping that has not been requested through a status packet, we can just ignore that case
            }
        }
    }
}
