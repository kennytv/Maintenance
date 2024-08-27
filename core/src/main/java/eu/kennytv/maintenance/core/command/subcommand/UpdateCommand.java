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
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.lib.kyori.adventure.text.event.ClickEvent;
import eu.kennytv.maintenance.lib.kyori.adventure.text.event.HoverEvent;
import eu.kennytv.maintenance.lib.kyori.adventure.text.format.NamedTextColor;
import eu.kennytv.maintenance.lib.kyori.adventure.text.minimessage.MiniMessage;

public final class UpdateCommand extends CommandInfo {

    public UpdateCommand(final MaintenancePlugin plugin) {
        super(plugin, "update");
    }

    @Override
    public void execute(final SenderInfo sender, final String[] args) {
        if (checkArgs(sender, args, 1)) return;
        if (args[0].equalsIgnoreCase("update")) {
            plugin.async(() -> checkForUpdate(sender));
            return;
        }

        if (!plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
            return;
        }

        sender.send(getMessage("updateDownloading"));
        try {
            if (plugin.installUpdate()) {
                sender.send(getMessage("updateFinished"));
            } else {
                sender.send(getMessage("updateFailed"));
            }
        } catch (Exception e) {
            sender.send(getMessage("updateFailed"));
            e.printStackTrace();
        }
    }

    private void checkForUpdate(final SenderInfo sender) {
        if (!plugin.updateAvailable()) {
            sender.sendMessage(plugin.getPrefix() + "§aYou already have the latest version of the plugin!");
            return;
        }

        sender.sendMessage(plugin.getPrefix() + "§cNewest version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
        sender.sendMessage(plugin.getPrefix() + "§c§lWARNING: §cYou will have to restart the server to prevent further issues and to complete the update! If you can't do that, don't update!");

        sender.send(Component.text().append(MiniMessage.miniMessage().deserialize("<gold>× <dark_gray>[<green>Update<dark_gray>]"))
                .clickEvent(ClickEvent.runCommand("/maintenance forceupdate"))
                .hoverEvent(HoverEvent.showText(Component.text().content("Click here to update the plugin").color(NamedTextColor.GREEN)))
                .append(Component.text().content(", or manually run ").color(NamedTextColor.GRAY))
                .append(Component.text().content("/maintenance forceupdate").color(NamedTextColor.RED)).build());
    }
}
