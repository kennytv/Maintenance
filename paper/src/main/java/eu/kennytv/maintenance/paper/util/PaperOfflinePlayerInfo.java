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
package eu.kennytv.maintenance.paper.util;

import eu.kennytv.maintenance.core.util.SenderInfo;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public final class PaperOfflinePlayerInfo implements SenderInfo {
    private final OfflinePlayer player;

    public PaperOfflinePlayerInfo(final OfflinePlayer player) {
        this.player = player;
    }

    @Override
    public UUID uuid() {
        return player.getUniqueId();
    }

    @Override
    public String name() {
        return player.getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        return false;
    }

    @Override
    public void send(final Component component) {
    }

    @Override
    public boolean isPlayer() {
        return true;
    }
}
