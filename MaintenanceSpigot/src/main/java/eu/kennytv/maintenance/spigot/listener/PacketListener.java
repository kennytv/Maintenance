package eu.kennytv.maintenance.spigot.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.google.common.collect.Lists;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotBase;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import eu.kennytv.maintenance.spigot.SettingsSpigot;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;
import java.util.UUID;

public final class PacketListener {
    private final File icon;
    private WrappedServerPing.CompressedImage image;

    public PacketListener(final MaintenanceSpigotPlugin pl, final MaintenanceSpigotBase base, final SettingsSpigot settings) {
        icon = new File("maintenance-icon.png");
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(base, ListenerPriority.HIGHEST,
                PacketType.Status.Server.OUT_SERVER_INFO) {
            @Override
            public void onPacketSending(final PacketEvent event) {
                if (!pl.isMaintenance()) return;

                final WrappedServerPing ping = event.getPacket().getServerPings().read(0);
                ping.setVersionProtocol(0);
                ping.setMotD(settings.getPingMessage().replace("%NEWLINE%", "\n"));
                ping.setVersionName(settings.getPlayerCountMessage());

                final List<WrappedGameProfile> players = Lists.newArrayList();
                for (final String string : settings.getPlayerCountHoverMessage().split("%NEWLINE%"))
                    players.add(new WrappedGameProfile(UUID.randomUUID(), string));
                ping.setPlayers(players);

                if (settings.hasCustomIcon()) {
                    if (image != null)
                        ping.setFavicon(image);
                    else {
                        try {
                            image = WrappedServerPing.CompressedImage.fromPng(ImageIO.read(icon));
                        } catch (final Exception e) {
                            pl.getServer().getLogger().warning("§4Could not load 'maintenance-icon.png' - did you create one in your Spigot/Bukkit folder (not the plugins folder)?");
                        }
                    }
                }
            }
        });
    }
}
