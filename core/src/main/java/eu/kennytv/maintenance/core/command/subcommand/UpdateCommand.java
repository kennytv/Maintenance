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

public final class UpdateCommand extends CommandInfo {

    public UpdateCommand(final MaintenancePlugin plugin) {
        super(plugin, "update");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 1)) return;
        if (args[0].equalsIgnoreCase("update")) {
            plugin.async(() -> plugin.getCommandManager().checkForUpdate(sender));
            return;
        }

        if (!plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
            return;
        }

        sender.send(getMessage("updateDownloading"));
        try {
            if (plugin.installUpdate()) {
                sender.send(getMessage("updateFinished"));
            } else {
                sender.send(getMessage("updateFailed"));
            }
        } catch (Exception e) {
            sender.send(getMessage("updateFailed"));
            e.printStackTrace();
        }
    }
}
