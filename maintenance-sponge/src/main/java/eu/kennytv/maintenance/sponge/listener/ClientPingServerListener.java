package eu.kennytv.maintenance.sponge.listener;

import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import eu.kennytv.maintenance.sponge.SettingsSponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class ClientPingServerListener implements IPingListener {
    private final MaintenanceSpongePlugin plugin;
    private final SettingsSponge settings;
    private Favicon favicon;

    public ClientPingServerListener(final MaintenanceSpongePlugin plugin, final SettingsSponge settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Listener(order = Order.LAST)
    public void proxyPing(final ClientPingServerEvent event) {
        if (!settings.isMaintenance()) return;

        final ClientPingServerEvent.Response response = event.getResponse();
        if (settings.hasCustomPlayerCountMessage()) {
            //TODO versionmessage
        }

        response.setDescription(Text.of(settings.getRandomPingMessage()));
        response.getPlayers().ifPresent(players -> {
            final List<GameProfile> profiles = players.getProfiles();
            profiles.clear();
            for (final String string : settings.getPlayerCountHoverMessage().split("%NEWLINE%"))
                profiles.add(GameProfile.of(UUID.randomUUID(), string));
        });

        if (settings.hasCustomIcon() && favicon != null)
            response.setFavicon(favicon);
    }

    @Override
    public boolean loadIcon() {
        try {
            favicon = Sponge.getGame().getRegistry().loadFavicon(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final IOException | IllegalArgumentException e) {
            plugin.getSpongeLogger().warn("§4Could not load 'maintenance-icon.png' - did you create one in your Sponge folder (not the plugins folder)?");
            if (settings.debugEnabled())
                e.printStackTrace();
            return false;
        }
        return true;
    }
}
