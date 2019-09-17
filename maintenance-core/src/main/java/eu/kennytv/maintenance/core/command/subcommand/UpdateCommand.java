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
import eu.kennytv.maintenance.core.util.ServerType;

public final class UpdateCommand extends CommandInfo {

    public UpdateCommand(final MaintenancePlugin plugin) {
        super(plugin, "update");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 1)) return;
        if (args[0].equalsIgnoreCase("update")) {
            plugin.async(() -> plugin.getCommandManager().checkForUpdate(sender));
        } else {
            if (!plugin.updateAvailable()) {
                sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
                return;
            }
            // Ore very sad :(
            if (plugin.getServerType() == ServerType.SPONGE) {
                sender.sendMessage(plugin.getPrefix() + "§cSorry, automated downloading of the plugin is not supported on Sponge! Please download the latest version manually! :(");
                return;
            }

            sender.sendMessage(getMessage("updateDownloading"));
            if (plugin.installUpdate())
                sender.sendMessage(getMessage("updateFinished"));
            else
                sender.sendMessage(getMessage("updateFailed"));
        }
    }
}
