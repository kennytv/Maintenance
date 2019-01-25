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

package eu.kennytv.maintenance.spigot.command;

import eu.kennytv.maintenance.core.command.MaintenanceCommand;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import eu.kennytv.maintenance.spigot.SettingsSpigot;
import eu.kennytv.maintenance.spigot.util.BukkitOfflinePlayerInfo;
import eu.kennytv.maintenance.spigot.util.BukkitSenderInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public final class MaintenanceSpigotCommand extends MaintenanceCommand implements CommandExecutor, TabCompleter {

    public MaintenanceSpigotCommand(final MaintenanceSpigotPlugin plugin, final SettingsSpigot settings) {
        super(plugin, settings);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String s, final String[] args) {
        execute(new BukkitSenderInfo(sender), args);
        return true;
    }

    @Override
    protected void addPlayerToWhitelist(final SenderInfo sender, final String name) {
        final Player selected = Bukkit.getPlayer(name);
        if (selected != null) {
            whitelistAddMessage(sender, new BukkitSenderInfo(selected));
            return;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(settings.getMessage("playerNotFound"));
            return;
        }

        whitelistAddMessage(sender, new BukkitOfflinePlayerInfo(offlinePlayer));
    }

    @Override
    protected void removePlayerFromWhitelist(final SenderInfo sender, final String name) {
        final Player selected = Bukkit.getPlayer(name);
        if (selected != null) {
            whitelistRemoveMessage(sender, new BukkitSenderInfo(selected));
            return;
        }

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (!offlinePlayer.hasPlayedBefore()) {
            sender.sendMessage(settings.getMessage("playerNotFound"));
            return;
        }

        whitelistRemoveMessage(sender, new BukkitOfflinePlayerInfo(offlinePlayer));
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return getSuggestions(new BukkitSenderInfo(sender), args);
    }
}
