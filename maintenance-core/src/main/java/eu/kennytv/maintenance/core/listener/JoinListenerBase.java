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
    private final Set<UUID> notifiedPlayers = new HashSet<>();
    private final UUID notifyUuid = new UUID(-6334418481592579467L, -4779835342378829761L);

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
    protected boolean handleLogin(final SenderInfo sender, final boolean notification) {
        if (notification && sender.getUuid().equals(notifyUuid))
            sender.sendMessage("§6Maintenance §aVersion " + plugin.getVersion());
        else if (settings.isMaintenance()) {
            if (!sender.hasMaintenancePermission("bypass") && !settings.getWhitelistedPlayers().containsKey(sender.getUuid())) {
                if (settings.isJoinNotifications())
                    broadcastJoinNotification(sender);
                return true;
            }
        }

        if (!settings.hasUpdateChecks()) return true;
        if (!sender.hasPermission("maintenance.admin") || notifiedPlayers.contains(sender.getUuid())) return false;

        plugin.async(() -> {
            if (!plugin.updateAvailable()) return;
            notifiedPlayers.add(sender.getUuid());
            plugin.sendUpdateNotification(sender);
        });
        return false;
    }

    protected boolean handleLogin(final SenderInfo sender) {
        return handleLogin(sender, true);
    }

    protected abstract void broadcastJoinNotification(SenderInfo sender);
}
