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
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MaintenanceDump {
    private final ServerDump general;
    private final Map<String, Object> configuration;
    private final JsonObject plugins;

    public MaintenanceDump(final MaintenancePlugin plugin, final Settings settings) {
        general = new ServerDump(plugin.getVersion(), plugin.getServerType().toString(), plugin.getServerVersion(), plugin.getMaintenanceServers());

        configuration = new LinkedHashMap<>(settings.getConfig().getValues());
        final Object o = configuration.get("mysql");
        if (o instanceof Map) {
            final Map<String, Object> map = new LinkedHashMap<>(((Map<String, Object>) o));
            map.keySet().removeIf(key -> !key.equals("use-mysql") && !key.equals("update-interval"));
            configuration.put("mysql", map);
        }
        configuration.put("whitelisted-players", settings.getWhitelistedPlayers());
        configuration.put("icon-exists", new File(plugin.getDataFolder(), "maintenance-icon.png").exists());

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("plugins", new GsonBuilder().create().toJsonTree(plugin.getPlugins()));
        plugins = jsonObject;
    }
}
