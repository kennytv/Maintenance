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
package eu.kennytv.maintenance.core.util;

import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.lib.kyori.adventure.text.minimessage.MiniMessage;
import java.util.UUID;

public interface SenderInfo {

    UUID getUuid();

    String getName();

    boolean hasPermission(String permission);

    default boolean hasMaintenancePermission(final String permission) {
        return permission == null || hasPermission("maintenance.admin") || hasPermission("maintenance." + permission);
    }

    @Deprecated
    void sendMessage(String message);

    void send(Component component);

    default void sendRich(final String message) {
        send(MiniMessage.miniMessage().deserialize(message));
    }

    boolean isPlayer();
}