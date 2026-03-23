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
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static eu.kennytv.maintenance.core.Settings.NEW_LINE_REPLACEMENT;

public final class SetMotdCommand extends CommandInfo {

    public SetMotdCommand(final MaintenancePlugin plugin) {
        super(plugin, "setmotd");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        int indexOffset = 1;
        boolean timerPingMessages = false;
        if (args.length > indexOffset && args[indexOffset].equalsIgnoreCase("timer")) {
            if (!getSettings().hasTimerSpecificPingMessages()) {
                sender.send(getMessage("timerMotdDisabled"));
                return;
            }

            indexOffset++;
            timerPingMessages = true;
        }

        if (args.length <= indexOffset + 2) {
            sender.send(getHelpMessage());
            return;
        }

        String mode = "default";
        if (!plugin.isNumeric(args[indexOffset])) {
            mode = args[indexOffset].toLowerCase(Locale.ROOT);
            indexOffset++;
        }

        if (args.length <= indexOffset + 2 || !plugin.isNumeric(args[indexOffset])) {
            sender.send(getHelpMessage());
            return;
        }

        final Settings settings = getSettings();
        final ConfigSection section = settings.getConfig().getSection("ping-message");
        final ConfigSection messageSection = section.getSection(timerPingMessages ? "timer-messages" : "messages");
        final List<String> pingMessages = getOrCreateModeMessages(messageSection, mode);

        final int index = Integer.parseInt(args[indexOffset]);
        if (index == 0 || index > pingMessages.size() + 1) {
            sender.send(getMessage("setMotdIndexError",
                    "%MOTDS%", Integer.toString(pingMessages.size()),
                    "%NEWAMOUNT%", Integer.toString(pingMessages.size() + 1)));
            return;
        }

        if (!plugin.isNumeric(args[indexOffset + 1])) {
            sender.send(getMessage("setMotdLineError"));
            return;
        }

        final int line = Integer.parseInt(args[indexOffset + 1]);
        if (line != 1 && line != 2) {
            sender.send(getMessage("setMotdLineError"));
            return;
        }

        final String message = String.join(" ", Arrays.copyOfRange(args, indexOffset + 2, args.length));
        final String oldMessage = index > pingMessages.size() ? "" : pingMessages.get(index - 1);
        final String newMessage;
        if (line == 1) {
            newMessage = oldMessage.contains(NEW_LINE_REPLACEMENT)
                    ? message + NEW_LINE_REPLACEMENT + oldMessage.split(NEW_LINE_REPLACEMENT, 2)[1]
                    : message;
        } else {
            newMessage = oldMessage.contains(NEW_LINE_REPLACEMENT)
                    ? oldMessage.split(NEW_LINE_REPLACEMENT, 2)[0] + NEW_LINE_REPLACEMENT + message
                    : oldMessage + NEW_LINE_REPLACEMENT + message;
        }

        if (index > pingMessages.size()) {
            pingMessages.add(newMessage);
        } else {
            pingMessages.set(index - 1, newMessage);
        }

        messageSection.set(mode, pingMessages);
        settings.saveConfig();
        settings.reloadConfigs();

        sender.send(settings.getMessage(
                "setMotd",
                "%LINE%", Integer.toString(line),
                "%INDEX%", Integer.toString(index),
                "%MOTD%", newMessage.replace(NEW_LINE_REPLACEMENT, "\n")
        ));
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        final int indexOffset;
        final boolean timer;
        if (args.length > 1 && args[1].equalsIgnoreCase("timer")) {
            timer = true;
            indexOffset = 2;
        } else {
            timer = false;
            indexOffset = 1;
        }

        if (args.length == indexOffset + 2) {
            return Arrays.asList("1", "2");
        }

        if (args.length == indexOffset + 1 && !plugin.isNumeric(args[indexOffset])) {
            return getModeCompletions(timer);
        }

        if (args.length == indexOffset + 1) {
            final String mode = plugin.isNumeric(args[indexOffset]) ? "default" : args[indexOffset].toLowerCase(Locale.ROOT);
            final ConfigSection section = getSettings().getConfig().getSection("ping-message").getSection(timer ? "timer-messages" : "messages");
            final int size = getOrCreateModeMessages(section, mode).size();
            final List<String> list = new ArrayList<>();
            for (int i = 1; i <= size + 1; i++) {
                list.add(String.valueOf(i));
            }
            return list;
        }
        return Collections.emptyList();
    }

    private List<String> getModeCompletions(final boolean timer) {
        final List<String> modes = new ArrayList<>(getSettings().getConfig().getSection("ping-message").getSection(timer ? "timer-messages" : "messages").getKeys());
        modes.remove("default");
        return modes;
    }

    private List<String> getOrCreateModeMessages(final ConfigSection section, final String mode) {
        final Object value = section.getObject(mode);
        if (value == null) {
            return new ArrayList<>();
        }

        if (value instanceof String s) {
            return new ArrayList<>(Collections.singletonList(s));
        }
        return new ArrayList<>(section.getStringList(mode));
    }
}
