package eu.kennytv.maintenance.bungee.listener;

import eu.kennytv.maintenance.bungee.MaintenanceBungeeBase;
import eu.kennytv.maintenance.bungee.SettingsBungee;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public final class ProxyPingListener implements Listener {
    private final MaintenanceBungeeBase plugin;
    private final SettingsBungee settings;
    private final File icon;
    private Favicon favicon;

    public ProxyPingListener(final MaintenanceBungeeBase plugin, final SettingsBungee settings) {
        this.plugin = plugin;
        this.settings = settings;
        icon = new File("maintenance-icon.png");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void proxyPing(final ProxyPingEvent event) {
        if (!settings.isMaintenance()) return;

        final ServerPing ping = event.getResponse();
        ping.setVersion(new ServerPing.Protocol(settings.getPlayerCountMessage(), 1));
        ping.setDescription(settings.getPingMessage().replace("%NEWLINE%", "\n"));
        ping.setPlayers(new ServerPing.Players(0, 0, new ServerPing.PlayerInfo[]{
                new ServerPing.PlayerInfo(settings.getPlayerCountHoverMessage().replace("%NEWLINE%", "\n"), "")
        }));

        if (settings.hasCustomIcon()) {
            if (favicon != null)
                ping.setFavicon(favicon);
            else {
                try {
                    favicon = Favicon.create(ImageIO.read(icon));
                } catch (IOException | IllegalArgumentException e) {
                    plugin.getLogger().warning("§4Could not load 'maintenance-icon.png' - did you create one in your Bungee folder (not the plugins folder)?");
                }
            }
        }
    }
}
