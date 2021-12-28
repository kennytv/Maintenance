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
package eu.kennytv.maintenance.velocity.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;

public final class VelocityServer implements Server {
    private final RegisteredServer server;

    public VelocityServer(final RegisteredServer server) {
        this.server = server;
    }

    @Override
    public String getName() {
        return server.getServerInfo().getName();
    }

    @Override
    public boolean hasPlayers() {
        return !server.getPlayersConnected().isEmpty();
    }

    @Override
    public void broadcast(final Component component) {
        final net.kyori.adventure.text.Component c = ComponentUtil.toVelocity(component);
        for (final Player player : server.getPlayersConnected()) {
            player.sendMessage(c);
        }
    }

    @Override
    public boolean isRegisteredServer() {
        return true;
    }

    public RegisteredServer getServer() {
        return server;
    }
}
