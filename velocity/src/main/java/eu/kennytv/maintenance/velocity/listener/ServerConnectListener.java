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
package eu.kennytv.maintenance.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.listener.ProxyJoinListenerBase;
import eu.kennytv.maintenance.core.proxy.util.ServerConnectResult;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.util.VelocitySenderInfo;
import eu.kennytv.maintenance.velocity.util.VelocityServer;
import java.util.Optional;
import net.kyori.adventure.text.Component;

public final class ServerConnectListener extends ProxyJoinListenerBase {
    private final MaintenanceVelocityPlugin plugin;

    public ServerConnectListener(final MaintenanceVelocityPlugin plugin, final SettingsProxy settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @Subscribe
    public void login(final LoginEvent event) {
        if (!event.getResult().isAllowed()) return;

        final VelocitySenderInfo sender = new VelocitySenderInfo(event.getPlayer());
        if (shouldKick(sender, false)) {
            final Server waitingServer = shouldConnectToWaitingServer(sender);
            // Do the actual connecting in the ServerPreConnectEvent handler if a waiting server exists
            if (waitingServer != null) return;

            event.setResult(ResultedEvent.ComponentResult.denied(settings.getKickMessage()));
            if (settings.isJoinNotifications()) {
                broadcastJoinNotification(event.getPlayer().getUsername());
            }
        }
    }

    @Subscribe
    public void postLogin(final PostLoginEvent event) {
        updateCheck(new VelocitySenderInfo(event.getPlayer()));
    }

    @SuppressWarnings("deprecation")
    @Subscribe(order = PostOrder.LAST)
    public void preConnect(final ServerPreConnectEvent event) {
        if (!event.getResult().isAllowed()) return;

        final Optional<RegisteredServer> optionalTarget = event.getResult().getServer();
        if (!optionalTarget.isPresent()) return;

        final Player player = event.getPlayer();
        final boolean hasCurrentServer = player.getCurrentServer().isPresent();
        final ServerConnectResult connectResult = serverConnect(new VelocitySenderInfo(player), new VelocityServer(optionalTarget.get()), hasCurrentServer);
        if (connectResult.isCancelled()) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

            // Player has no server to connect to
            if (!hasCurrentServer) {
                player.disconnect(settings.getKickMessage());
            }
        } else if (connectResult.getTarget() != null) {
            final RegisteredServer newTarget = ((VelocityServer) connectResult.getTarget()).getServer();
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(newTarget));
        }
    }

    @Override
    protected void broadcastJoinNotification(final String name) {
        sendJoinMessage(plugin.getServer().getAllPlayers(), name);
    }

    @Override
    protected void broadcastJoinNotification(final String name, final Server server) {
        sendJoinMessage(((VelocityServer) server).getServer().getPlayersConnected(), name);
    }

    private void sendJoinMessage(final Iterable<Player> players, final String name) {
        final Component message = settings.getMessage("joinNotification", "%PLAYER%", name);
        for (final Player player : players) {
            if (plugin.hasPermission(player, "joinnotification")) {
                player.sendMessage(message);
            }
        }
    }
}
