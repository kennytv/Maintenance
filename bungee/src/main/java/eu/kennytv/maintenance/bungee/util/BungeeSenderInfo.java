/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2022 kennytv (https://github.com/kennytv)
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

import eu.kennytv.maintenance.api.MaintenanceProvider;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.bungee.MaintenanceBungeePlugin;
import eu.kennytv.maintenance.core.proxy.util.ProxySenderInfo;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public final class BungeeSenderInfo implements ProxySenderInfo {
    private final CommandSender sender;

    public BungeeSenderInfo(final CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public UUID getUuid() {
        return sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    @Deprecated
    public void sendMessage(final String message) {
        sender.sendMessage(message);
    }

    @Override
    public void send(final Component component) {
        ((MaintenanceBungeePlugin) MaintenanceProvider.get()).audiences().sender(sender).sendMessage(component);
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof ProxiedPlayer;
    }

    public void sendMessage(final TextComponent textComponent) {
        sender.sendMessage(textComponent);
    }

    @Override
    public boolean canAccess(final Server server) {
        return ((BungeeServer) server).getServer().canAccess(sender);
    }

    @Override
    public void disconnect(final Component component) {
        if (sender instanceof Connection) {
            ((Connection) sender).disconnect(ComponentUtil.toBadComponent(component));
        }
    }
}
