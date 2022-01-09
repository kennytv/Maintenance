/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2022 kennytv (https://github.com/kennytv)
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
package eu.kennytv.maintenance.bungee.listener;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.bungee.MaintenanceBungeePlugin;
import eu.kennytv.maintenance.bungee.util.BungeeSenderInfo;
import eu.kennytv.maintenance.bungee.util.BungeeServer;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.listener.ProxyJoinListenerBase;
import eu.kennytv.maintenance.core.proxy.util.ServerConnectResult;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public final class ServerConnectListener extends ProxyJoinListenerBase implements Listener {
    private final MaintenanceBungeePlugin plugin;

    public ServerConnectListener(final MaintenanceBungeePlugin plugin, final SettingsProxy settings) {
        super(plugin, settings);
        this.plugin = plugin;
    }

    @EventHandler
    public void initialServerConnect(final ServerConnectEvent event) {
        // Global maintenance check
        if (event.isCancelled() || event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY) return;

        final BungeeSenderInfo player = new BungeeSenderInfo(event.getPlayer());
        if (plugin.isDebug()) {
            plugin.getLogger().info("Join permission check for " + event.getPlayer().getName()
                    + " - Permission: " + player.hasMaintenancePermission("bypass") + ", whitelist: " + settings.isWhitelisted(player.getUuid()));
        }

        if (shouldKick(player)) {
            final Server waitingServer = shouldConnectToWaitingServer(player);
            if (waitingServer != null) {
                event.setTarget(((BungeeServer) waitingServer).getServer());
                player.send(settings.getMessage("sentToWaitingServer"));

                if (plugin.isDebug()) {
                    plugin.getLogger().info("Join to waiting server for " + event.getPlayer().getName());
                }
                return;
            }

            event.setCancelled(true);

            player.disconnect(settings.getKickMessage());
            if (settings.isJoinNotifications()) {
                broadcastJoinNotification(player.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void serverConnect(final ServerConnectEvent event) {
        // Server specific maintenance check
        if (event.isCancelled()) return;

        final ProxiedPlayer proxiedPlayer = event.getPlayer();
        final boolean normalServerConnect = event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY && event.getReason() != ServerConnectEvent.Reason.KICK_REDIRECT
                && event.getReason() != ServerConnectEvent.Reason.LOBBY_FALLBACK && event.getReason() != ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT;
        final BungeeSenderInfo player = new BungeeSenderInfo(proxiedPlayer);
        final ServerConnectResult connectResult = serverConnect(player, new BungeeServer(event.getTarget()), normalServerConnect);
        if (plugin.isDebug()) {
            plugin.getLogger().info("Connectresult for " + player.getName() + " to " + event.getTarget().getName() + ": " + connectResult);
        }

        if (connectResult.isCancelled()) {
            event.setCancelled(true);

            // Player has no server to connect to
            if (proxiedPlayer.getServer() == null) {
                player.disconnect(settings.getKickMessage());
            }
        } else if (connectResult.getTarget() != null) {
            event.setTarget(((BungeeServer) connectResult.getTarget()).getServer());
        }
    }

    @Override
    protected void broadcastJoinNotification(final String name) {
        sendJoinMessage(ProxyServer.getInstance().getPlayers(), name);
    }

    @Override
    protected void broadcastJoinNotification(final String name, final Server server) {
        sendJoinMessage(((BungeeServer) server).getServer().getPlayers(), name);
    }

    private void sendJoinMessage(final Iterable<ProxiedPlayer> players, final String name) {
        final Component component = settings.getMessage("joinNotification", "%PLAYER%", name);
        for (final ProxiedPlayer player : players) {
            if (plugin.hasPermission(player, "joinnotification")) {
                plugin.audiences().player(player).sendMessage(component);
            }
        }
    }
}
