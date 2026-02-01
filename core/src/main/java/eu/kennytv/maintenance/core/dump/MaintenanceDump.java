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
package eu.kennytv.maintenance.core.dump;

import com.google.gson.JsonObject;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public final class MaintenanceDump {
    private final ServerDump general;
    private final Map<String, Object> configuration;
    private final JsonObject plugins;

    public MaintenanceDump(final MaintenancePlugin plugin, final Settings settings) {
        general = new ServerDump(plugin.getVersion(), plugin.getServerType().toString(), plugin.getServerVersion(), plugin.getMaintenanceServersDump());

        configuration = new LinkedHashMap<>(settings.getConfig().getValues());
        addDBSettings("redis", "enabled");
        configuration.put("whitelisted-players", settings.getWhitelistedPlayers());
        configuration.put("icon-exists", new File(plugin.getDataFolder(), "maintenance-icon.png").exists());

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("plugins", MaintenancePlugin.GSON.toJsonTree(plugin.getPlugins()));
        plugins = jsonObject;
    }

    private void addDBSettings(final String key, final String... allowedSubKeys) {
        final Set<String> allowedSubKeysSet = Set.of(allowedSubKeys);
        final Object o = configuration.get(key);
        if (o instanceof Map) {
            final Map<String, Object> map = new LinkedHashMap<>(((Map<String, Object>) o));
            map.keySet().removeIf(subKey -> !allowedSubKeysSet.contains(subKey));
            configuration.put(key, map);
        }
    }
}
