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

package eu.kennytv.maintenance.sponge.listener;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
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
        final Text text = plugin.translate(settings.getMessage("joinNotification").replace("%PLAYER%", name));
        for (final Player p : Sponge.getServer().getOnlinePlayers()) {
            if (plugin.hasPermission(p, "joinnotification")) {
                p.sendMessage(text);
            }
        }
    }
}
