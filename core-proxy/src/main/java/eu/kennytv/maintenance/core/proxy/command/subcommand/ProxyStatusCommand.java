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
package eu.kennytv.maintenance.core.proxy.command.subcommand;

import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.command.ProxyCommandInfo;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnable;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.Map;

public final class ProxyStatusCommand extends ProxyCommandInfo {

    public ProxyStatusCommand(final MaintenanceProxyPlugin plugin) {
        super(plugin, "singleserver.status");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        final MaintenanceRunnable task = plugin.getCurrentTask();
        if (task != null) {
            final String key = task.shouldEnable() ? "maintenanceStatusOffWithStartTimer" : "maintenanceStatusOnWithEndTimer";
            sender.send(getMessage(key, "%TIME%", task.getTime()));
        } else {
            sender.send(getMessage(plugin.isMaintenance() ? "maintenanceStatusOn" : "maintenanceStatusOff"));
        }

        if (getSettings().getMaintenanceServers().isEmpty() && !plugin.hasServerTasks()) {
            sender.send(getMessage("singleServerMaintenanceListEmpty"));
            return;
        }

        sender.send(getMessage("singleServerMaintenanceList"));
        // Enabled servers first
        for (final String server : getSettings().getMaintenanceServers()) {
            final MaintenanceRunnableBase serverTask = plugin.getServerTask(server);
            if (serverTask != null) {
                sender.send(getMessage("singleServerMaintenanceListEntryWithEndTimer", "%SERVER%", server, "%TIME%", serverTask.getTime()));
                continue;
            }

            sender.send(getMessage("singleServerMaintenanceListEntry", "%SERVER%", server));
        }

        // Then enabling tasks
        for (final Map.Entry<String, MaintenanceRunnableBase> entry : plugin.getServerTasks().entrySet()) {
            final MaintenanceRunnableBase serverTask = entry.getValue();
            if (serverTask.shouldEnable()) {
                sender.send(getMessage("singleServerMaintenanceListEntryWithStartTimer", "%SERVER%", entry.getKey(), "%TIME%", serverTask.getTime()));
            }
        }
    }
}
