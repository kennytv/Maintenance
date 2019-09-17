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
import eu.kennytv.maintenance.core.command.CommandInfo;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class WhitelistRemoveCommand extends CommandInfo {

    public WhitelistRemoveCommand(final MaintenancePlugin plugin) {
        super(plugin, "whitelist.remove");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 2)) return;
        if (args[1].length() == 36) {
            final UUID uuid = plugin.checkUUID(sender, args[1]);
            if (uuid != null)
                removePlayerFromWhitelist(sender, uuid);
        } else
            removePlayerFromWhitelist(sender, args[1]);
    }

    @Override
    public List<String> getTabCompletion(final SenderInfo sender, final String[] args) {
        if (args.length != 2) return Collections.emptyList();

        final List<String> list = new ArrayList<>();
        for (final String name : getSettings().getWhitelistedPlayers().values()) {
            if (name.toLowerCase().startsWith(args[1])) {
                list.add(name);
            }
        }
        return list;
    }

    private void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final SenderInfo selected = plugin.getOfflinePlayer(name);
        if (selected == null) {
            if (getSettings().removeWhitelistedPlayer(name))
                sender.sendMessage(getMessage("whitelistRemoved").replace("%PLAYER%", name));
            else
                sender.sendMessage(getMessage("whitelistNotFound"));
            return;
        }

        whitelistRemoveMessage(sender, selected.getUuid(), selected.getName());
    }

    private void removePlayerFromWhitelist(final SenderInfo sender, final UUID uuid) {
        final SenderInfo selected = plugin.getOfflinePlayer(uuid);
        if (selected == null)
            whitelistRemoveMessage(sender, uuid, uuid.toString());
        else
            whitelistRemoveMessage(sender, selected.getUuid(), selected.getName());
    }

    private void whitelistRemoveMessage(final SenderInfo sender, final UUID uuid, final String toReplace) {
        if (getSettings().removeWhitelistedPlayer(uuid))
            sender.sendMessage(getMessage("whitelistRemoved").replace("%PLAYER%", toReplace));
        else
            sender.sendMessage(getMessage("whitelistNotFound"));
    }
}
