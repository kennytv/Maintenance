package eu.kennytv.maintenance.spigot.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotBase;
import eu.kennytv.maintenance.spigot.SettingsSpigot;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PacketListener implements IPingListener {
    private final MaintenanceSpigotBase pl;
    private WrappedServerPing.CompressedImage image;

    public PacketListener(final MaintenanceSpigotBase base, final SettingsSpigot settings) {
        this.pl = base;
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
                            .replace("%ONLINE%", Integer.toString(pl.getServer().getOnlinePlayers().size()))
                            .replace("%MAX%", Integer.toString(pl.getServer().getMaxPlayers())));
                }

                final List<WrappedGameProfile> players = new ArrayList<>();
                for (final String string : settings.getPlayerCountHoverMessage().split("%NEWLINE%"))
                    players.add(new WrappedGameProfile(UUID.randomUUID(), string));
                ping.setPlayers(players);

                if (settings.hasCustomIcon() && image != null)
                    ping.setFavicon(image);
            }
        });
    }

    @Override
    public boolean loadIcon() {
        try {
            image = WrappedServerPing.CompressedImage.fromPng(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final Exception e) {
            pl.getLogger().warning("§4Could not load 'maintenance-icon.png' - did you create one in your Spigot folder (not the plugins folder)?");
            return false;
        }
        return true;
    }
}
