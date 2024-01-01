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
package eu.kennytv.maintenance.spigot.listener;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import eu.kennytv.maintenance.spigot.util.BukkitSenderInfo;
import eu.kennytv.maintenance.spigot.util.ComponentUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public final class PlayerLoginListener extends JoinListenerBase implements Listener {
    private final MaintenanceSpigotPlugin plugin;

    public PlayerLoginListener(final MaintenanceSpigotPlugin plugin, final Settings settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @EventHandler
    public void postLogin(final PlayerLoginEvent event) {
        final BukkitSenderInfo sender = new BukkitSenderInfo(event.getPlayer());
        if (shouldKick(sender)) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            if (ComponentUtil.PAPER) {
                event.kickMessage(ComponentUtil.toPaperComponent(settings.getKickMessage()));
            } else {
                event.setKickMessage(ComponentUtil.toLegacy(settings.getKickMessage()));
            }
            if (settings.isJoinNotifications()) {
                broadcastJoinNotification(sender.getName());
            }
        }
    }

    @Override
    protected void broadcastJoinNotification(final String name) {
        for (final Player p : plugin.getServer().getOnlinePlayers()) {
            if (plugin.hasPermission(p, "joinnotification")) {
                plugin.audiences().player(p).sendMessage(settings.getMessage("joinNotification", "%PLAYER%", name));
            }
        }
    }
}
