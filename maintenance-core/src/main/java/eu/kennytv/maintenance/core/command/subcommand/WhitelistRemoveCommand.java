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

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    protected String[] helpMessage() {
        return fromStrings("§6/maintenance remove <name/uuid> §7(Removes the player from the maintenance whitelist)");
    }

    @Override
    public List<String> getTabCompletion(final String[] args) {
        return args.length == 2 ? getSettings().getWhitelistedPlayers().values().stream()
                .filter(name -> name.toLowerCase().startsWith(args[1])).collect(Collectors.toList()) : Collections.emptyList();
    }

    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final SenderInfo selected = plugin.getPlayer(name);
        if (selected == null) {
            if (getSettings().removeWhitelistedPlayer(name))
                sender.sendMessage(getMessage("whitelistRemoved").replace("%PLAYER%", name));
            else
                sender.sendMessage(getMessage("whitelistNotFound"));
            return;
        }

        whitelistRemoveMessage(sender, selected);
    }

    protected void removePlayerFromWhitelist(final SenderInfo sender, final UUID uuid) {
        final SenderInfo selected = plugin.getOfflinePlayer(uuid);
        if (selected == null) {
            if (getSettings().removeWhitelistedPlayer(uuid))
                sender.sendMessage(getMessage("whitelistRemoved").replace("%PLAYER%", uuid.toString()));
            else
                sender.sendMessage(getMessage("whitelistNotFound"));
            return;
        }

        whitelistRemoveMessage(sender, selected);
    }

    protected void whitelistRemoveMessage(final SenderInfo sender, final SenderInfo selected) {
        if (getSettings().removeWhitelistedPlayer(selected.getUuid()))
            sender.sendMessage(getMessage("whitelistRemoved").replace("%PLAYER%", selected.getName()));
        else
            sender.sendMessage(getMessage("whitelistNotFound"));
    }
}
