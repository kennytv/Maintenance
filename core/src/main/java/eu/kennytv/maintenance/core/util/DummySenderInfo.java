/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
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

import java.util.UUID;

public final class DummySenderInfo implements SenderInfo {
    private final UUID uuid;
    private final String name;

    public DummySenderInfo(final UUID uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasPermission(final String permission) {
        return false;
    }

    @Override
    @Deprecated
    public void sendMessage(final String message) {
    }

    @Override
    public void send(final Component component) {
    }

    @Override
    public boolean isPlayer() {
        return false;
    }
}
