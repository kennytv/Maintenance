/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2021 KennyTV (https://github.com/KennyTV)
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

package eu.kennytv.maintenance.bungee.util;

import eu.kennytv.maintenance.api.proxy.Server;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class BungeeServer implements Server {
    private final ServerInfo server;

    public BungeeServer(final ServerInfo server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return server.getName();
    }

    @Override
    public boolean hasPlayers() {
        return !server.getPlayers().isEmpty();
    }

    @Override
    public void broadcast(final String message) {
        final BaseComponent[] s = TextComponent.fromLegacyText(message);
        for (final ProxiedPlayer p : server.getPlayers()) {
            p.sendMessage(s);
        }
    }

    @Override
    public boolean isRegisteredServer() {
        return true;
    }

    public ServerInfo getServer() {
        return server;
    }
}
