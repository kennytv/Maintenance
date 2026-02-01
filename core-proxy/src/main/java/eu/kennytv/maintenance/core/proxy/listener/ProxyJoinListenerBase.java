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
package eu.kennytv.maintenance.core.proxy.listener;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.listener.JoinListenerBase;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.core.proxy.SettingsProxy;
import eu.kennytv.maintenance.core.proxy.util.ProxySenderInfo;
import eu.kennytv.maintenance.core.proxy.util.ServerConnectResult;
import eu.kennytv.maintenance.core.util.SenderInfo;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public abstract class ProxyJoinListenerBase extends JoinListenerBase {
    private static final ServerConnectResult ALLOWED = new ServerConnectResult(false);
    private static final ServerConnectResult DENIED = new ServerConnectResult(true);
    protected final MaintenanceProxyPlugin plugin;
    protected final SettingsProxy settings;
    private boolean warned;

    protected ProxyJoinListenerBase(final MaintenanceProxyPlugin plugin, final SettingsProxy settings) {
        super(plugin, settings);
        this.plugin = plugin;
        this.settings = settings;
    }

    /**
     * @param sender              joined player
     * @param target              target server
     * @param normalServerConnect true if normal server connect, false if initial proxy join or kick
     * @return connect result with data for further handling
     */
    protected ServerConnectResult serverConnect(final ProxySenderInfo sender, final Server target, final boolean normalServerConnect) {
        // Check waiting server for global maintenance
        if (settings.isMaintenance()) {
            if (sender.hasMaintenancePermission("bypass") || settings.isWhitelisted(sender.uuid())) return ALLOWED;

            final Server waitingServer = shouldConnectToWaitingServer(sender);
            // Should never be null, but just in case
            if (waitingServer == null) return DENIED;

            // Player is connecting to the waiting server
            if (target.getName().equals(waitingServer.getName())) return ALLOWED;

            // Player already is on the waiting server
            final String currentServer = plugin.getServerNameOf(sender);
            if (waitingServer.getName().equals(currentServer)) {
                sender.send(settings.getMessage("forceWaitingServer"));
                return DENIED;
            }

            sender.send(settings.getMessage("sentToWaitingServer"));
            return new ServerConnectResult(waitingServer);
        }

        // Single server maintenance
        if (!settings.isMaintenance(target.getName())) return ALLOWED;
        if (sender.hasMaintenancePermission("bypass") || settings.isWhitelisted(sender.uuid())
                || sender.hasMaintenancePermission("singleserver.bypass." + target.getName().toLowerCase(Locale.ROOT))) {
            return ALLOWED;
        }

        if (settings.isJoinNotifications()) {
            broadcastJoinNotification(sender.name(), target);
        }

        if (normalServerConnect) {
            sender.send(settings.getServerKickMessage(target.getName()));
            return DENIED;
        }

        // If it's the initial proxy join or a kick from another server, go back to fallback server
        final Server fallback = settings.getFallbackServer();
        if (fallback == null || !sender.canAccess(fallback)) {
            // Nothing to redirect to, player has to be kicked from the proxy
            sender.disconnect(settings.getFullServerKickMessage(target.getName()));
            if (!warned) {
                plugin.getLogger().warning("Could not send player to the set fallback server; instead kicking player off the network!");
                warned = true;
            }
            return DENIED;
        } else {
            return new ServerConnectResult(fallback);
        }
    }

    /**
     * To be called after {@link #shouldKick(SenderInfo, boolean)} to get the waiting server if present.
     * Does not do any permission checks, they should be done separately!
     *
     * @param sender sender
     * @return waiting server, null if player should be kicked
     */
    @Nullable
    protected Server shouldConnectToWaitingServer(final ProxySenderInfo sender) {
        if (settings.getWaitingServer() == null) return null;

        final Server waitingServer = plugin.getServer(settings.getWaitingServer());
        if (waitingServer == null) return null;
        if (!sender.canAccess(waitingServer) || settings.isMaintenance(waitingServer.getName())) return null;

        return waitingServer;
    }

    protected abstract void broadcastJoinNotification(String name, Server server);
}
