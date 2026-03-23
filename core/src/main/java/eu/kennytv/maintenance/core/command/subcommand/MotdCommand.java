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
import eu.kennytv.maintenance.core.config.ConfigSection;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public final class MotdCommand extends CommandInfo {

    public MotdCommand(final MaintenancePlugin plugin) {
        super(plugin, "motd");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        int indexOffset = 1;
        boolean timer = false;
        if (args.length > indexOffset && args[indexOffset].equalsIgnoreCase("timer")) {
            if (!getSettings().hasTimerSpecificPingMessages()) {
                sender.send(getMessage("timerMotdDisabled"));
                return;
            }

            timer = true;
            indexOffset++;
        }

        if (args.length > indexOffset + 1) {
            sender.send(getHelpMessage());
            return;
        }

        final String mode = args.length == indexOffset + 1 ? args[indexOffset].toLowerCase(Locale.ROOT) : "default";
        final ConfigSection section = getSettings().getConfig().getSection("ping-message").getSection(timer ? "timer-messages" : "messages");
        sendList(sender, getModeMessages(section, mode));
    }

    private void sendList(final SenderInfo sender, @Nullable final List<String> list) {
        if (list == null || list.isEmpty()) {
            sender.send(getMessage("motdListEmpty"));
            return;
        }

        sender.send(getMessage("motdList"));
        for (int i = 0; i < list.size(); i++) {
            sender.sendRich("<aqua>" + (i + 1) + "<dark_gray><st>---------");
            sender.sendRich(list.get(i).replace("<br>", "\n"));
        }
        sender.sendRich("<dark_gray><st>----------");
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length < 2 || args.length > 3) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            final List<String> list = new ArrayList<>();
            if (getSettings().hasTimerSpecificPingMessages()) {
                list.add("timer");
            }

            final List<String> modes = new ArrayList<>(getSettings().getConfig().getSection("ping-message").getSection("messages").getKeys());
            modes.remove("default");
            list.addAll(modes);
            return list;
        }

        if (!args[1].equalsIgnoreCase("timer") || !getSettings().hasTimerSpecificPingMessages()) {
            return Collections.emptyList();
        }

        final List<String> modes = new ArrayList<>(getSettings().getConfig().getSection("ping-message").getSection("timer-messages").getKeys());
        modes.remove("default");
        return modes;
    }

    private @Nullable List<String> getModeMessages(final ConfigSection section, final String mode) {
        final Object value = section.getObject(mode);
        if (value instanceof String s) {
            return Collections.singletonList(s);
        }
        return section.getStringList(mode);
    }
}
