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

public final class ServerInfoPacketListener implements Listener {
    private final UUID uuid = new UUID(0, 0);
    private final MaintenanceSpigotPlugin plugin;
    private final Settings settings;
    //private WrappedServerPing.CompressedImage image;

    public ServerInfoPacketListener(final MaintenanceSpigotPlugin plugin, final MaintenanceSpigotBase base, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(base, ListenerPriority.HIGHEST,
                PacketType.Status.Server.SERVER_INFO) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                if (!settings.isMaintenance()) return;

                final WrappedServerPing ping = event.getPacket().getServerPings().read(0);
                ping.setMotD(settings.getRandomPingMessage());

                if (settings.hasCustomPlayerCountMessage()) {
                    ping.setVersionProtocol(0);
                    ping.setVersionName(settings.getPlayerCountMessage()
                            .replace("%ONLINE%", Integer.toString(base.getServer().getOnlinePlayers().size()))
                            .replace("%MAX%", Integer.toString(base.getServer().getMaxPlayers())));
                }

                final List<WrappedGameProfile> players = new ArrayList<>();
                for (final String string : settings.getPlayerCountHoverMessage().split("%NEWLINE%"))
                    players.add(new WrappedGameProfile(uuid, string));
                ping.setPlayers(players);
                //if (settings.hasCustomIcon() && image != null) ping.setFavicon(image);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverListPing(final ServerListPingEvent event) {
        if (settings.isMaintenance() && settings.hasCustomIcon() && plugin.getFavicon() != null)
            event.setServerIcon(plugin.getFavicon());
    }

    /*@Override
    public boolean loadIcon() {
        try {
            image = WrappedServerPing.CompressedImage.fromPng(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final Exception e) {
            pl.getLogger().warning("Could not load 'maintenance-icon.png' - did you create one in your Spigot folder (not the plugins folder)?");
            if (pl.getApi().getSettings().debugEnabled())
                e.printStackTrace();
            return false;
        }
        return true;
    }*/
}
