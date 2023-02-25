/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2022 kennytv (https://github.com/kennytv)
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
        super(plugin, "setmotd");
    }

    @Override
    public void execute(final SenderInfo sender, String[] args) {
        boolean timerPingMessages = false;
        if (args.length == 3 && args[1].equalsIgnoreCase("timer")) {
            if (!getSettings().hasTimerSpecificPingMessages()) {
                sender.send(getMessage("timerMotdDisabled"));
                return;
            }

            args = plugin.removeArrayIndex(args, 1);
            timerPingMessages = true;
        }
        if (checkArgs(sender, args, 2)) return;
        if (!plugin.isNumeric(args[1])) {
            sender.send(getHelpMessage());
            return;
        }

        final Settings settings = getSettings();
        final List<String> pingMessages = settings.getConfig().getStringList(timerPingMessages ? "timerspecific-pingmessages" : "pingmessages");
        if (pingMessages.size() < 2) {
            sender.send(getMessage("removeMotdError"));
            return;
        }

        final int index = Integer.parseInt(args[1]);
        if (index == 0 || index > pingMessages.size()) {
            sender.send(getMessage("setMotdIndexError",
                    "%MOTDS%", Integer.toString(pingMessages.size()),
                    "%NEWAMOUNT%", Integer.toString(pingMessages.size())));
            return;
        }

        final List<String> pingComponents = timerPingMessages ? settings.getTimerSpecificPingMessages() : settings.getPingMessages();
        pingComponents.remove(index - 1);
        pingMessages.remove(index - 1);
        settings.getConfig().set(timerPingMessages ? "timerspecific-pingmessages" : "pingmessages", pingMessages);
        settings.saveConfig();
        sender.send(getMessage("removedMotd", "%INDEX%", args[1]));
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length == 2 || (args.length == 3 && args[1].equalsIgnoreCase("timer"))) {
            final int size = (args.length == 3 ? plugin.getSettings().getTimerSpecificPingMessages() : plugin.getSettings().getPingMessages()).size();
            final List<String> list = new ArrayList<>(size);
            for (int i = 1; i <= size; i++) {
                list.add(Integer.toString(i));
            }
            return list;
        }
        return Collections.emptyList();
    }
}
