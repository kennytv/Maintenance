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
package eu.kennytv.maintenance.core.proxy.command;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.command.subcommand.*;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class MaintenanceProxyCommand extends MaintenanceCommand {
    private final MaintenanceProxyPlugin plugin;
    private final SettingsProxy settingsBungee;

    protected MaintenanceProxyCommand(final MaintenanceProxyPlugin plugin, final SettingsProxy settings) {
        super(plugin, settings);
        this.plugin = plugin;
        settingsBungee = settings;
    }

    @Override
    protected void addToggleAndTimerCommands() {
        add(new SingleToggleCommand(plugin), "on", "off");
        add(new StatusCommand(plugin), "status");

        add(new SingleStarttimerCommand(plugin), "starttimer", "start");
        add(new SingleEndtimerCommand(plugin), "endtimer", "end");
        add(new SingleScheduleTimerCommand(plugin), "scheduletimer", "schedule");
        add(new AbortSingleTimerCommand(plugin), "aborttimer", "abort");
    }

    @Override
    public List<String> getMaintenanceServersCompletion(final String s) {
        final List<String> list = new ArrayList<>();
        for (final String server : settingsBungee.getMaintenanceServers()) {
            if (server.toLowerCase(Locale.ROOT).startsWith(s)) {
                list.add(server);
            }
        }
        return list;
    }

    public Server checkSingleTimerServerArg(final SenderInfo sender, final String serverName) {
        final Server server = plugin.getServer(serverName);
        if (server == null) {
            sender.send(settings.getMessage("serverNotFound"));
            return null;
        }
        if (plugin.isServerTaskRunning(server)) {
            sender.send(settings.getMessage("singleTimerAlreadyRunning"));
            return null;
        }
        return server;
    }
}
