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

public final class MotdCommand extends CommandInfo {

    public MotdCommand(final MaintenancePlugin plugin) {
        super(plugin, "motd");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 1)) return;
        sender.sendMessage(getMessage("motdList"));
        for (int i = 0; i < getSettings().getPingMessages().size(); i++) {
            sender.sendMessage("§b" + (i + 1) + "§8§m---------");
            for (final String motd : getSettings().getColoredString(getSettings().getPingMessages().get(i)).split("%NEWLINE%")) {
                sender.sendMessage(motd);
            }
        }
        sender.sendMessage("§8§m----------");
    }

    @Override
    protected String[] helpMessage() {
        return fromStrings("§6/maintenance motd §7(Lists the currently set maintenance motds)");
    }
}
