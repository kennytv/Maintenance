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

package eu.kennytv.maintenance.core.proxy.command.subcommand;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.command.ProxyCommandInfo;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.Collections;
import java.util.List;

public final class SingleEndtimerCommand extends ProxyCommandInfo {

    public SingleEndtimerCommand(final MaintenanceProxyPlugin plugin) {
        super(plugin, null, "§6/maintenance endtimer [server] <minutes> §7(After the given time in minutes, maintenance mode will be disabled)");
    }

    @Override
    public boolean hasPermission(final SenderInfo sender) {
        return sender.hasMaintenancePermission("timer") || sender.hasPermission("maintenance.servertimer");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (args.length == 2) {
            if (checkPermission(sender, "timer")) return;
            if (plugin.getCommandManager().checkTimerArgs(sender, args[1], "endtimerUsage")) return;
            if (!plugin.isMaintenance()) {
                sender.sendMessage(getMessage("alreadyDisabled"));
                return;
            }
            plugin.startMaintenanceRunnable(Integer.parseInt(args[1]), false);
            sender.sendMessage(getMessage("endtimerStarted").replace("%TIME%", plugin.getRunnable().getTime()));
        } else if (args.length == 3) {
            if (checkPermission(sender, "servertimer")) return;
            if (plugin.getCommandManager().checkTimerArgs(sender, args[2], "singleEndtimerUsage", false)) return;

            final Server server = plugin.getCommandManager().checkSingleTimerArgs(sender, args);
            if (server == null) return;
            if (!plugin.isMaintenance(server)) {
                sender.sendMessage(getMessage("singleServerAlreadyDisabled").replace("%SERVER%", server.getName()));
                return;
            }
            final MaintenanceRunnableBase runnable = plugin.startSingleMaintenanceRunnable(server, Integer.parseInt(args[2]), false);
            sender.sendMessage(getMessage("endtimerStarted").replace("%TIME%", runnable.getTime()));
        } else
            sender.sendMessage(helpMessage);
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        return args.length == 2 && sender.hasMaintenancePermission("servertimer") ? plugin.getCommandManager().getMaintenanceServersCompletion(args[1].toLowerCase()) : Collections.emptyList();
    }
}
