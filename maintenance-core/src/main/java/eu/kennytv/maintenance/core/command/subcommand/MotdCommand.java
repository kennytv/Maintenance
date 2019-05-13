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

import java.util.List;

public final class MotdCommand extends CommandInfo {

    public MotdCommand(final MaintenancePlugin plugin) {
        super(plugin, "motd", "§6/maintenance motd §7(Lists the currently set maintenance motds)");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 1)) return;
        if (args.length == 1) {
            sender.sendMessage(getMessage("motdList"));
            sendList(sender, getSettings().getPingMessages());
            sender.sendMessage("§8§m----------");
        } else if (args.length == 2) {

        } else
            sender.sendMessage(helpMessage);

    }

    private void sendList(final SenderInfo sender, final List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            sender.sendMessage("§b" + (i + 1) + "§8§m---------");
            for (final String motd : getSettings().getColoredString(list.get(i)).split("%NEWLINE%")) {
                sender.sendMessage(motd);
            }
        }
    }
}
