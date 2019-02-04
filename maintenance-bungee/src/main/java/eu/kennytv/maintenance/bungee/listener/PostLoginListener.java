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
import eu.kennytv.maintenance.bungee.util.BungeeSenderInfo;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class PostLoginListener extends JoinListenerBase implements Listener {
    private final MaintenanceBungeePlugin plugin;

    public PostLoginListener(final MaintenanceBungeePlugin plugin, final Settings settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void postLogin(final PostLoginEvent event) {
        //TODO LoginEvent?
        if (handleLogin(new BungeeSenderInfo(event.getPlayer())))
            event.getPlayer().disconnect(settings.getKickMessage());
    }

    @Override
    protected void broadcastJoinNotification(final SenderInfo sender) {
        final BaseComponent[] s = TextComponent.fromLegacyText(settings.getMessage("joinNotification").replace("%PLAYER%", sender.getName()));
        ProxyServer.getInstance().getPlayers().stream().filter(p -> plugin.hasPermission(p, "joinnotification")).forEach(p -> p.sendMessage(s));
    }
}
