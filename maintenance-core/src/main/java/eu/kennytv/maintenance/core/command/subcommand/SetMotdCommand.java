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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SetMotdCommand extends CommandInfo {

    public SetMotdCommand(final MaintenancePlugin plugin) {
        super(plugin, "setmotd", "§6/maintenance setmotd <index> <1/2> <message> §7(Sets a motd for maintenance mode)");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (args.length < 4 || !plugin.isNumeric(args[1])) {
            sender.sendMessage(helpMessage);
            return;
        }

        final Settings settings = getSettings();
        final int index = Integer.parseInt(args[1]);
        if (index == 0 || index > settings.getPingMessages().size() + 1) {
            sender.sendMessage(getMessage("setMotdIndexError").replace("%MOTDS%", Integer.toString(settings.getPingMessages().size()))
                    .replace("%NEWAMOUNT%", Integer.toString(settings.getPingMessages().size() + 1)));
            return;
        }

        if (!plugin.isNumeric(args[2])) {
            sender.sendMessage(getMessage("setMotdLineError"));
            return;
        }

        final int line = Integer.parseInt(args[2]);
        if (line != 1 && line != 2) {
            sender.sendMessage(getMessage("setMotdLineError"));
            return;
        }

        final String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        final String oldMessage = index > settings.getPingMessages().size() ? "" : settings.getPingMessages().get(index - 1);
        final String newMessage;
        if (line == 1)
            newMessage = oldMessage.contains("%NEWLINE%") ?
                    message + "%NEWLINE%" + oldMessage.split("%NEWLINE%", 2)[1] : message;
        else
            newMessage = oldMessage.contains("%NEWLINE%") ?
                    oldMessage.split("%NEWLINE%", 2)[0] + "%NEWLINE%" + message : oldMessage + "%NEWLINE%" + message;

        if (index > settings.getPingMessages().size())
            settings.getPingMessages().add(newMessage);
        else
            settings.getPingMessages().set(index - 1, newMessage);
        settings.getConfig().set("pingmessages", settings.getPingMessages());
        settings.saveConfig();
        sender.sendMessage(settings.getMessage("setMotd").replace("%LINE%", args[2]).replace("%INDEX%", args[1])
                .replace("%MOTD%", "§f" + settings.getColoredString(message)));
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length == 3) return Arrays.asList("1", "2");
        if (args.length == 2) {
            final List<String> list = new ArrayList<>();
            for (int i = 1; i <= getSettings().getPingMessages().size() + 1; i++) {
                list.add(String.valueOf(i));
            }
            return list;
        }
        return Collections.emptyList();
    }
}
