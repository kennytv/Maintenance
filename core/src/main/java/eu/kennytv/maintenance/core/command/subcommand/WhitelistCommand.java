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
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;

public final class WhitelistCommand extends CommandInfo {

    public WhitelistCommand(final MaintenancePlugin plugin) {
        super(plugin, "whitelist.list");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 1)) return;

        final Map<UUID, String> players = getSettings().getWhitelistedPlayers();
        if (players.isEmpty()) {
            sender.send(getMessage("whitelistEmpty"));
            return;
        }

        sender.send(getMessage("whitelistedPlayers"));
        final Component format = getMessage("whitelistedPlayersFormat");
        for (final Map.Entry<UUID, String> entry : players.entrySet()) {
            sender.send(format.replaceText(builder -> builder.matchLiteral("%NAME%").replacement(entry.getValue()))
                    .replaceText(builder -> builder.matchLiteral("%UUID%").replacement(entry.getKey().toString())));
        }

        sender.send(Component.empty());
    }
}
