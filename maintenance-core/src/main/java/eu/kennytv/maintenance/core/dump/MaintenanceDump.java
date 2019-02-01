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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;

import java.util.Map;

public final class MaintenanceDump {
    private final ServerDump general;
    private final Map<String, Object> configuration;
    private final JsonObject plugins;

    public MaintenanceDump(final MaintenanceModePlugin plugin) {
        general = new ServerDump(plugin.getVersion(), plugin.getServerType().toString(), plugin.getServerVersion(), plugin.getMaintenanceServers());

        // TODO when changing to simpleconfig (?)
        configuration = null;

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("plugins", new GsonBuilder().create().toJsonTree(plugin.getPlugins()));
        plugins = jsonObject;
    }
}
