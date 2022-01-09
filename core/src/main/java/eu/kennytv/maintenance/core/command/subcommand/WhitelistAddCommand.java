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
import eu.kennytv.maintenance.core.util.DummySenderInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class WhitelistAddCommand extends CommandInfo {

    public WhitelistAddCommand(final MaintenancePlugin plugin) {
        super(plugin, "whitelist.add");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (args.length == 2) {
            if (args[1].length() == 36) {
                final UUID uuid = plugin.checkUUID(sender, args[1]);
                if (uuid != null) {
                    addPlayerToWhitelist(sender, uuid);
                }
            } else {
                addPlayerToWhitelist(sender, args[1]);
            }
        } else if (args.length == 3) {
            if (args[1].length() != 36) {
                sender.send(getHelpMessage());
                return;
            }

            final UUID uuid = plugin.checkUUID(sender, args[1]);
            if (uuid != null) {
                addPlayerToWhitelist(sender, new DummySenderInfo(uuid, args[2]));
            }
        } else {
            sender.send(getHelpMessage());
        }
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        return args.length == 2 ? plugin.getCommandManager().getPlayersCompletion() : Collections.emptyList();
    }

    private void addPlayerToWhitelist(final SenderInfo sender, final String name) {
        plugin.getOfflinePlayer(name, selected -> {
            if (selected == null) {
                sender.send(getMessage("playerNotOnline"));
                return;
            }

            addPlayerToWhitelist(sender, selected);
        });
    }

    private void addPlayerToWhitelist(final SenderInfo sender, final UUID uuid) {
        plugin.getOfflinePlayer(uuid, selected -> {
            if (selected == null) {
                sender.send(getMessage("playerNotFoundUuid"));
                return;
            }

            addPlayerToWhitelist(sender, selected);
        });
    }

    private void addPlayerToWhitelist(final SenderInfo sender, final SenderInfo selected) {
        if (getSettings().addWhitelistedPlayer(selected.getUuid(), selected.getName())) {
            sender.send(getMessage("whitelistAdded", "%PLAYER%", selected.getName()));
        } else {
            sender.send(getMessage("whitelistAlreadyAdded", "%PLAYER%", selected.getName()));
        }
    }
}
