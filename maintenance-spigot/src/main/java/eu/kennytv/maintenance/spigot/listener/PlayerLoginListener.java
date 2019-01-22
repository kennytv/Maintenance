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

package eu.kennytv.maintenance.spigot.listener;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.util.SenderInfo;
import eu.kennytv.maintenance.spigot.util.BukkitSenderInfo;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PlayerLoginListener extends JoinListenerBase implements Listener {

    public PlayerLoginListener(final MaintenanceModePlugin plugin, final Settings settings) {
        super(plugin, settings);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void postLogin(final PlayerLoginEvent event) {
        if (handleLogin(new BukkitSenderInfo(event.getPlayer()))) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(settings.getKickMessage().replace("%NEWLINE%", "\n"));
        }
    }

    @Override
    protected void broadcastJoinNotification(final SenderInfo sender) {
        Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("maintenance.joinnotification"))
                .forEach(p -> p.sendMessage(settings.getMessage("joinNotification").replace("%PLAYER%", sender.getName())));
    }
}
