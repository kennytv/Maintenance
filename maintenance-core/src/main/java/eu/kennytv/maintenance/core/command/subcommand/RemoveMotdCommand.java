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
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RemoveMotdCommand extends CommandInfo {

    public RemoveMotdCommand(final MaintenancePlugin plugin) {
        super(plugin, "setmotd", "§6/maintenance removemotd <index> §7(Removes a maintenance motd)");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 2)) return;
        if (!plugin.isNumeric(args[1])) {
            sender.sendMessage(getMessage("removeMotdUsage"));
            return;
        }

        final Settings settings = getSettings();
        if (settings.getPingMessages().size() < 2) {
            sender.sendMessage(getMessage("removeMotdError"));
            return;
        }

        final int index = Integer.parseInt(args[1]);
        if (index > settings.getPingMessages().size()) {
            sender.sendMessage(getMessage("setMotdIndexError").replace("%MOTDS%", Integer.toString(settings.getPingMessages().size()))
                    .replace("%NEWAMOUNT%", Integer.toString(settings.getPingMessages().size())));
            return;
        }

        settings.getPingMessages().remove(index - 1);
        settings.getConfig().set("pingmessages", settings.getPingMessages());
        settings.saveConfig();
        sender.sendMessage(getMessage("removedMotd").replace("%INDEX%", args[1]));
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length != 2) return Collections.emptyList();
        final int size = plugin.getSettings().getPingMessages().size();
        final List<String> list = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }
}
