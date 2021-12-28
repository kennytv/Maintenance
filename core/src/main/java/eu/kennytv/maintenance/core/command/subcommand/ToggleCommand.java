/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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

public final class ToggleCommand extends CommandInfo {

    public ToggleCommand(final MaintenancePlugin plugin) {
        super(plugin, "toggle");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 1)) return;

        final boolean maintenance = args[0].equalsIgnoreCase("on");
        if (maintenance == plugin.isMaintenance()) {
            sender.send(getMessage(maintenance ? "alreadyEnabled" : "alreadyDisabled"));
            return;
        }

        plugin.setMaintenance(maintenance);
    }
}
