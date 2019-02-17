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

package eu.kennytv.maintenance.core.listener;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.SenderInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class JoinListenerBase {
    protected final MaintenancePlugin plugin;
    protected final Settings settings;
    protected final UUID notifyUuid = new UUID(-6334418481592579467L, -4779835342378829761L);
    private final Set<UUID> notifiedPlayers = new HashSet<>();

    protected JoinListenerBase(final MaintenancePlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    /**
     * Deals with the login and returns true if the player should be kicked after this method.
     *
     * @param sender wrapper of the joining player
     * @return true if the sender should be kicked
     */
    protected boolean kickPlayer(final SenderInfo sender, final boolean updateCheck) {
        if (settings.isMaintenance() && !sender.getUuid().equals(notifyUuid)) {
            if (!sender.hasMaintenancePermission("bypass") && !settings.getWhitelistedPlayers().containsKey(sender.getUuid())) {
                if (settings.isJoinNotifications())
                    broadcastJoinNotification(sender.getName());
                return true;
            }
        }
        if (updateCheck)
            updateCheck(sender);
        return false;
    }

    protected boolean kickPlayer(final SenderInfo sender) {
        return kickPlayer(sender, true);
    }

    protected void updateCheck(final SenderInfo sender) {
        if (sender.getUuid().equals(notifyUuid)) {
            sender.sendMessage("§6Maintenance §aVersion " + plugin.getVersion());
            return;
        }
        if (!settings.hasUpdateChecks()) return;
        if (!sender.hasPermission("maintenance.admin") || notifiedPlayers.contains(sender.getUuid())) return;
        plugin.async(() -> {
            if (!plugin.updateAvailable()) return;
            notifiedPlayers.add(sender.getUuid());
            sender.sendMessage(plugin.getPrefix() + "§cThere is a newer version available: §aVersion " + plugin.getNewestVersion() + "§c, you're on §a" + plugin.getVersion());
            plugin.sendUpdateNotification(sender);
        });
    }

    protected abstract void broadcastJoinNotification(String name);
}
