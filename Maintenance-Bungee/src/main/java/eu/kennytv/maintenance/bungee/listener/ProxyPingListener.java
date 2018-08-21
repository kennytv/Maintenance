package eu.kennytv.maintenance.bungee.listener;

import eu.kennytv.maintenance.bungee.MaintenanceBungeeBase;
import eu.kennytv.maintenance.bungee.SettingsBungee;
import eu.kennytv.maintenance.core.listener.IPingListener;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public final class ProxyPingListener implements Listener, IPingListener {
    private final MaintenanceBungeeBase plugin;
    private final SettingsBungee settings;
    private Favicon favicon;

    public ProxyPingListener(final MaintenanceBungeeBase plugin, final SettingsBungee settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void proxyPing(final ProxyPingEvent event) {
        if (!settings.isMaintenance()) return;

        final ServerPing ping = event.getResponse();
        ping.getVersion().setProtocol(1);
        ping.getVersion().setName(settings.getPlayerCountMessage()
                .replace("%ONLINE%", Integer.toString(ping.getPlayers().getOnline()))
                .replace("%MAX%", Integer.toString(ping.getPlayers().getMax())));
        ping.setDescription(settings.getRandomPingMessage());
        ping.getPlayers().setOnline(0);
        ping.getPlayers().setMax(0);
        ping.getPlayers().setSample(new ServerPing.PlayerInfo[]{
                new ServerPing.PlayerInfo(settings.getPlayerCountHoverMessage().replace("%NEWLINE%", "\n"), "")
        });

        if (settings.hasCustomIcon() && favicon != null)
            ping.setFavicon(favicon);
    }

    @Override
    public boolean loadIcon() {
        try {
            favicon = Favicon.create(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final IOException | IllegalArgumentException e) {
            plugin.getLogger().warning("§4Could not load 'maintenance-icon.png' - did you create one in your Bungee folder (not the plugins folder)?");
            return false;
        }
        return true;
    }
}
