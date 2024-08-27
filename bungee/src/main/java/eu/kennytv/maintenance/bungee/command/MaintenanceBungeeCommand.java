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
package eu.kennytv.maintenance.bungee.command;

import eu.kennytv.maintenance.bungee.MaintenanceBungeePlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.command.MaintenanceProxyCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class MaintenanceBungeeCommand extends MaintenanceProxyCommand {
    private final MaintenanceBungeePlugin plugin;

    public MaintenanceBungeeCommand(final MaintenanceBungeePlugin plugin, final SettingsProxy settings) {
        super(plugin, settings);
        this.plugin = plugin;
        registerCommands();
    }

    @Override
    public List<String> getServersCompletion(final String s) {
        final List<String> list = new ArrayList<>();
        for (final Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
            final String serverName = entry.getValue().getName();
            if (entry.getKey().toLowerCase().startsWith(s) && !plugin.getSettingsProxy().getMaintenanceServers().contains(serverName)) {
                list.add(serverName);
            }
        }
        return list;
    }

    @Override
    public List<String> getPlayersCompletion() {
        final List<String> list = new ArrayList<>();
        for (final ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            list.add(p.getName());
        }
        return list;
    }
}
