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
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.SettingsVelocity;
import net.kyori.text.TextComponent;

public final class ServerConnectListener implements EventHandler<ServerPreConnectEvent> {
    private final MaintenanceVelocityPlugin plugin;
    private final SettingsVelocity settings;

    public ServerConnectListener(final MaintenanceVelocityPlugin plugin, final SettingsVelocity settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void execute(final ServerPreConnectEvent event) {
        if (!event.getResult().isAllowed()) return;

        final Player player = event.getPlayer();
        final RegisteredServer target = event.getResult().getServer().get();
        if (!plugin.isMaintenance(target.getServerInfo())) return;
        if (player.hasPermission("maintenance.bypass") || settings.getWhitelistedPlayers().containsKey(player.getUniqueId()))
            return;

        event.setResult(ServerPreConnectEvent.ServerResult.denied());
        if (settings.isJoinNotifications()) {
            final TextComponent s = TextComponent.of(settings.getMessage("joinNotification").replace("%PLAYER%", player.getUsername()));
            target.getPlayersConnected().stream().filter(p -> p.hasPermission("maintenance.joinnotification")).forEach(p -> p.sendMessage(s));
        }
        // Normal serverconnect
        /*if (event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY && event.getReason() != ServerConnectEvent.Reason.KICK_REDIRECT
                && event.getReason() != ServerConnectEvent.Reason.LOBBY_FALLBACK && event.getReason() != ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT) {
            player.sendMessage(settings.getMessage("singleMaintenanceKick").replace("%SERVER%", target.getName()));
            return;
        }

        // If it's the initial proxy join or a kick from another server, go back to fallback server
        final ServerInfo fallback = plugin.getProxy().getServerInfo(settings.getFallbackServer());
        if (fallback == null || !fallback.canAccess(p) || plugin.isMaintenance(fallback)) {
            player.disconnect(settings.getMessage("singleMaintenanceKickComplete").replace("%NEWLINE%", "\n").replace("%SERVER%", target.getName()));
            if (!warned) {
                plugin.getLogger().warning("Could not send player to the fallback server set in the SpigotServers.yml! Instead kicking player off the network!");
                warned = true;
            }
        } else
            player.connect(fallback);*/
    }
}
