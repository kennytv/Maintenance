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

import com.destroystokyo.paper.profile.PlayerProfile;
import eu.kennytv.maintenance.core.util.SenderInfo;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.connection.PlayerLoginConnection;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class PaperConnectionInfo implements SenderInfo {
    private final PlayerConnection connection;

    public PaperConnectionInfo(final PlayerConnection connection) {
        this.connection = connection;
    }

    @Override
    public UUID uuid() {
        return profile().getId();
    }

    @Override
    public String name() {
        return profile().getName();
    }

    @Override
    public boolean hasPermission(final String permission) {
        // TODO needs to be possible
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(final Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPlayer() {
        return connection instanceof Player;
    }

    private PlayerProfile profile() {
        // TODO Verify this once used
        if (connection instanceof PlayerLoginConnection loginConnection) {
            return loginConnection.getAuthenticatedProfile();
        } else if (connection instanceof PlayerConfigurationConnection configurationConnection) {
            return configurationConnection.getProfile();
        }
        throw new IllegalArgumentException("Unknown connection type: " + connection.getClass());
    }
}
