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

package eu.kennytv.maintenance.velocity.listener;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import net.kyori.text.TextComponent;

import java.util.Optional;

public final class ServerConnectListener implements EventHandler<ServerPreConnectEvent> {
    private final MaintenanceVelocityPlugin plugin;
    private final SettingsProxy settings;
    private boolean warned;

    public ServerConnectListener(final MaintenanceVelocityPlugin plugin, final SettingsProxy settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void execute(final ServerPreConnectEvent event) {
        if (!event.getResult().isAllowed()) return;

        final Player player = event.getPlayer();
        if (!event.getResult().getServer().isPresent()) return;

        final RegisteredServer target = event.getResult().getServer().get();
        if (!plugin.isMaintenance(target)) return;
        if (plugin.hasPermission(player, "bypass") || settings.getWhitelistedPlayers().containsKey(player.getUniqueId())
                || plugin.hasPermission(player, "singleserver.bypass." + target.getServerInfo().getName().toLowerCase()))
            return;

        event.setResult(ServerPreConnectEvent.ServerResult.denied());
        if (settings.isJoinNotifications()) {
            final TextComponent s = plugin.translate(settings.getMessage("joinNotification").replace("%PLAYER%", player.getUsername()));
            target.getPlayersConnected().stream().filter(p -> plugin.hasPermission(p, "joinnotification")).forEach(p -> p.sendMessage(s));
        }

        // Normal serverconnect
        if (player.getCurrentServer().isPresent()) {
            player.sendMessage(plugin.translate(settings.getMessage("singleMaintenanceKick").replace("%SERVER%", target.getServerInfo().getName())));
            return;
        }

        // If it's the initial proxy join or a kick from another server, go back to fallback server
        final Optional<RegisteredServer> fallback = plugin.getServer().getServer(settings.getFallbackServer());
        if (!fallback.isPresent() || plugin.isMaintenance(fallback.get())) {
            disconnect(player, target);
            if (!warned) {
                plugin.getLogger().warning("Could not send player to the set fallback server! Instead kicking player off the network!");
                warned = true;
            }
        } else {
            player.createConnectionRequest(fallback.get()).connect().whenComplete((result, throwable) -> {
                if (!result.isSuccessful()) disconnect(player, target);
            });
        }
    }

    private void disconnect(final Player player, final RegisteredServer target) {
        player.disconnect(TextComponent.of(settings.getMessage("singleMaintenanceKickComplete").replace("%NEWLINE%", "\n")
                .replace("%SERVER%", target.getServerInfo().getName())));
    }
}
