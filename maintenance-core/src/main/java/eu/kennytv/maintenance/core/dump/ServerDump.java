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

package eu.kennytv.maintenance.core.dump;

import java.util.List;

public final class ServerDump {
    private final String pluginVersion;
    private final String platform;
    private final String serverVersion;
    private final List<String> maintenance;

    public ServerDump(final String pluginVersion, final String platform, final String serverVersion, final List<String> maintenance) {
        this.pluginVersion = pluginVersion;
        this.platform = platform;
        this.serverVersion = serverVersion;
        this.maintenance = maintenance;
    }
}