/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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
package eu.kennytv.maintenance.sponge.listener;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import eu.kennytv.maintenance.sponge.util.SpongeSenderInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public final class ClientConnectionListener extends JoinListenerBase {
    private final MaintenanceSpongePlugin plugin;

    public ClientConnectionListener(final MaintenanceSpongePlugin plugin, final Settings settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @Listener
    public void login(final ClientConnectionEvent.Login event) {
        final Player player = event.getTargetUser().getPlayer().get();
        if (shouldKick(new SpongeSenderInfo(player), false)) {
            event.setCancelled(true);
            event.setMessage(Text.of(settings.getKickMessage()));
            if (settings.isJoinNotifications()) {
                broadcastJoinNotification(player.getName());
            }
        }
    }

    @Listener
    public void join(final ClientConnectionEvent.Join event) {
        updateCheck(new SpongeSenderInfo(event.getTargetEntity()));
    }

    @Override
    protected void broadcastJoinNotification(final String name) {
        final Component component = settings.getMessage("joinNotification", "%PLAYER%", name);
        for (final Player player : Sponge.getServer().getOnlinePlayers()) {
            if (plugin.hasPermission(player, "joinnotification")) {
                plugin.audiences().player(player).sendMessage(component);
            }
        }
    }
}
