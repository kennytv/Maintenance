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
package eu.kennytv.maintenance.core.command.subcommand;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.time.Duration;

public final class ScheduleTimerCommand extends CommandInfo {

    public ScheduleTimerCommand(final MaintenancePlugin plugin) {
        super(plugin, "timer");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 3)) return;

        final Duration enableIn = plugin.getCommandManager().parseDurationAndCheckTask(sender, args[1]);
        final Duration maintenanceDuration = plugin.getCommandManager().parseDurationAndCheckTask(sender, args[2], false);
        if (enableIn == null || maintenanceDuration == null) {
            sender.send(getHelpMessage());
            return;
        }
        if (plugin.isMaintenance()) {
            sender.send(getMessage("alreadyEnabled"));
            return;
        }

        plugin.scheduleMaintenanceRunnable(enableIn, maintenanceDuration);
        sender.send(getMessage(
                "scheduletimerStarted",
                "%TIME%", plugin.getRunnable().getTime(),
                "%DURATION%", plugin.getFormattedTime((int) maintenanceDuration.getSeconds())
        ));
    }
}
