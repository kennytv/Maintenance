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

import eu.kennytv.maintenance.bungee.util.ProxiedSenderInfo;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class PostLoginListener extends JoinListenerBase implements Listener {

    public PostLoginListener(final MaintenanceModePlugin plugin, final Settings settings) {
        super(plugin, settings);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void postLogin(final PostLoginEvent event) {
        if (handleLogin(new ProxiedSenderInfo(event.getPlayer())))
            event.getPlayer().disconnect(settings.getKickMessage().replace("%NEWLINE%", "\n"));
    }

    @Override
    protected void broadcastJoinNotification(final SenderInfo sender) {
        ProxyServer.getInstance().getPlayers().stream().filter(p -> p.hasPermission("maintenance.joinnotification"))
                .forEach(p -> p.sendMessage(settings.getMessage("joinNotification").replace("%PLAYER%", sender.getName())));
    }
}
