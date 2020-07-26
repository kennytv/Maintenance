/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2020 KennyTV (https://github.com/KennyTV)
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
import java.util.UUID;

public final class WhitelistAddCommand extends CommandInfo {

    public WhitelistAddCommand(final MaintenancePlugin plugin) {
        super(plugin, "whitelist.add");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 2)) return;
        if (args[1].length() == 36) {
            final UUID uuid = plugin.checkUUID(sender, args[1]);
            if (uuid != null) {
                addPlayerToWhitelist(sender, uuid);
            }
        } else {
            addPlayerToWhitelist(sender, args[1]);
        }
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        return args.length == 2 ? plugin.getCommandManager().getPlayersCompletion() : Collections.emptyList();
    }

    private void addPlayerToWhitelist(final SenderInfo sender, final String name) {
        final SenderInfo selected = plugin.getOfflinePlayer(name);
        if (selected == null) {
            sender.sendMessage(getMessage("playerNotOnline"));
            return;
        }

        whitelistAddMessage(sender, selected);
    }

    private void addPlayerToWhitelist(final SenderInfo sender, final UUID uuid) {
        final SenderInfo selected = plugin.getOfflinePlayer(uuid);
        if (selected == null) {
            sender.sendMessage(getMessage("playerNotFoundUuid"));
            return;
        }

        whitelistAddMessage(sender, selected);
    }

    private void whitelistAddMessage(final SenderInfo sender, final SenderInfo selected) {
        if (getSettings().addWhitelistedPlayer(selected.getUuid(), selected.getName())) {
            sender.sendMessage(getMessage("whitelistAdded").replace("%PLAYER%", selected.getName()));
        } else {
            sender.sendMessage(getMessage("whitelistAlreadyAdded").replace("%PLAYER%", selected.getName()));
        }
    }
}
