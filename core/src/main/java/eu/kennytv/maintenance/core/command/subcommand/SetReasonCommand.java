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

import java.util.Collections;
import java.util.List;

public final class SetReasonCommand extends CommandInfo {

    public SetReasonCommand(final MaintenancePlugin plugin) {
        super(plugin, "setreason");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (args.length < 2) {
            sender.send(getHelpMessage());
            return;
        }

        final String value = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        final String reason;
        if (value.equalsIgnoreCase("none") || value.equalsIgnoreCase("clear") || value.equals("-")) {
            reason = null;
        } else {
            reason = value;
        }

        getSettings().setActiveReason(reason);
        sender.send(getMessage("setReason", "%REASON%", reason == null ? "-" : reason));
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length != 2) {
            return Collections.emptyList();
        }
        return Collections.singletonList("none");
    }
}
