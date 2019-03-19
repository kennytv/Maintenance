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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotBase;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ServerInfoPacketListener extends PacketAdapter implements Listener {
    private final UUID uuid = new UUID(0, 0);
    private final MaintenanceSpigotPlugin plugin;
    private final Settings settings;

    public ServerInfoPacketListener(final MaintenanceSpigotPlugin plugin, final MaintenanceSpigotBase base, final Settings settings) {
        super(base, ListenerPriority.HIGHEST, PacketType.Status.Server.SERVER_INFO);
        this.plugin = plugin;
        this.settings = settings;
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        if (!settings.isMaintenance()) return;

        final WrappedServerPing ping = event.getPacket().getServerPings().read(0);
        ping.setMotD(settings.getRandomPingMessage());

        if (settings.hasCustomPlayerCountMessage()) {
            ping.setVersionProtocol(1);
            ping.setVersionName(settings.getPlayerCountMessage()
                    .replace("%ONLINE%", Integer.toString(plugin.getServer().getOnlinePlayers().size()))
                    .replace("%MAX%", Integer.toString(plugin.getServer().getMaxPlayers())));
        }

        final List<WrappedGameProfile> players = new ArrayList<>();
        for (final String string : settings.getPlayerCountHoverMessage().split("%NEWLINE%"))
            players.add(new WrappedGameProfile(uuid, string));
        ping.setPlayers(players);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverListPing(final ServerListPingEvent event) {
        // Set the icon here, not in the packet listener, as it's broken for 1.13+ clients on older server versions
        if (settings.isMaintenance() && settings.hasCustomIcon() && plugin.getFavicon() != null) {
            try {
                event.setServerIcon(plugin.getFavicon());
            } catch (final UnsupportedOperationException ignored) {
                // Thrown in a ping that has not been requested through a status packet, we can just ignore that case
            }
        }
    }
}
