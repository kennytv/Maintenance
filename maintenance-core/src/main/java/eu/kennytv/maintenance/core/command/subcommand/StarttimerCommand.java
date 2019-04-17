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

package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;

public final class StarttimerCommand extends CommandInfo {

    public StarttimerCommand(final MaintenancePlugin plugin) {
        super(plugin, "timer", "§6/maintenance starttimer <minutes> §7(After the given time in minutes, maintenance mode will be enabled)");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 2)) return;
        if (plugin.getCommandManager().checkTimerArgs(sender, args[1])) {
            sender.sendMessage(helpMessage);
            return;
        }
        if (plugin.isMaintenance()) {
            sender.sendMessage(getMessage("alreadyEnabled"));
            return;
        }

        plugin.startMaintenanceRunnableForMinutes(Integer.parseInt(args[1]), true);
        sender.sendMessage(getMessage("starttimerStarted").replace("%TIME%", plugin.getRunnable().getTime()));
    }
}
