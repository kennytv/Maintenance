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
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.lib.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.kennytv.maintenance.core.Settings.NEW_LINE_REPLACEMENT;

public final class SetMotdCommand extends CommandInfo {

    public SetMotdCommand(final MaintenancePlugin plugin) {
        super(plugin, "setmotd");
    }

    @Override
    public void execute(final SenderInfo sender, String[] args) {
        boolean timerPingMessages = false;
        if (args.length > 1 && args[1].equalsIgnoreCase("timer")) {
            if (!getSettings().hasTimerSpecificPingMessages()) {
                sender.send(getMessage("timerMotdDisabled"));
                return;
            }

            // remove the "timer" off the args to keep the rest the code cleaner
            args = plugin.removeArrayIndex(args, 1);
            timerPingMessages = true;
        }
        if (args.length < 4 || !plugin.isNumeric(args[1])) {
            sender.send(getHelpMessage());
            return;
        }

        final Settings settings = getSettings();
        final List<Component> pingComponents = timerPingMessages ? settings.getTimerSpecificPingMessages() : settings.getPingMessages();
        final List<String> pingMessages = settings.getConfig().getStringList(timerPingMessages ? "timerspecific-pingmessages" : "pingmessages");
        final int index = Integer.parseInt(args[1]);
        if (index == 0 || index > pingMessages.size() + 1) {
            sender.send(getMessage("setMotdIndexError",
                    "%MOTDS%", Integer.toString(pingMessages.size()),
                    "%NEWAMOUNT%", Integer.toString(pingMessages.size() + 1)));
            return;
        }

        if (!plugin.isNumeric(args[2])) {
            sender.send(getMessage("setMotdLineError"));
            return;
        }

        final int line = Integer.parseInt(args[2]);
        if (line != 1 && line != 2) {
            sender.send(getMessage("setMotdLineError"));
            return;
        }

        final String message = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
        final String oldMessage = index > pingMessages.size() ? "" : pingMessages.get(index - 1);
        final String newMessage;
        if (line == 1) {
            newMessage = oldMessage.contains(NEW_LINE_REPLACEMENT) ?
                    message + NEW_LINE_REPLACEMENT + oldMessage.split(NEW_LINE_REPLACEMENT, 2)[1] : message;
        } else {
            newMessage = oldMessage.contains(NEW_LINE_REPLACEMENT) ?
                    oldMessage.split(NEW_LINE_REPLACEMENT, 2)[0] + NEW_LINE_REPLACEMENT + message : oldMessage + NEW_LINE_REPLACEMENT + message;
        }

        // Replace component in config and the cached list
        final Component component = MiniMessage.miniMessage().deserialize(newMessage.replace(NEW_LINE_REPLACEMENT, "\n"));
        if (index > pingMessages.size()) {
            pingMessages.add(newMessage);
            pingComponents.add(component);
        } else {
            pingMessages.set(index - 1, newMessage);
            pingComponents.set(index - 1, component);
        }

        settings.getConfig().set(timerPingMessages ? "timerspecific-pingmessages" : "pingmessages", pingMessages);
        settings.saveConfig();
        sender.send(settings.getMessage("setMotd",
                "%LINE%", args[2],
                "%INDEX%", args[1]).replaceText(builder -> builder.matchLiteral("%MOTD%").replacement(component)));
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, String[] args) {
        boolean timer = false;
        if (args.length > 1 && args[1].equalsIgnoreCase("timer")) {
            args = plugin.removeArrayIndex(args, 1);
            timer = true;
        }
        if (args.length == 3) return Arrays.asList("1", "2");
        if (args.length == 2) {
            final List<String> list = new ArrayList<>();
            for (int i = 1; i <= (timer ? getSettings().getTimerSpecificPingMessages() : getSettings().getPingMessages()).size() + 1; i++) {
                list.add(String.valueOf(i));
            }
            return list;
        }
        return Collections.emptyList();
    }
}
