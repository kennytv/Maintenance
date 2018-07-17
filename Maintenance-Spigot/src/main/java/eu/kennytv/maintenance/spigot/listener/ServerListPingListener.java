package eu.kennytv.maintenance.spigot.listener;

import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotBase;
import eu.kennytv.maintenance.spigot.SettingsSpigot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.io.File;

public final class ServerListPingListener implements Listener, IPingListener {
    private final MaintenanceSpigotBase plugin;
    private final SettingsSpigot settings;
    private CachedServerIcon serverIcon;

    public ServerListPingListener(final MaintenanceSpigotBase plugin, final SettingsSpigot settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverListPing(final ServerListPingEvent event) {
        if (!settings.isMaintenance()) return;

        event.setMaxPlayers(0);
        event.setMotd(settings.getRandomPingMessage());

        if (settings.hasCustomIcon() && serverIcon != null)
            event.setServerIcon(serverIcon);
    }

    @Override
    public boolean loadIcon() {
        try {
            serverIcon = plugin.getServer().loadServerIcon(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final Exception e) {
            plugin.getLogger().warning("§4Could not load 'maintenance-icon.png' - did you create one in your Spigot folder (not the plugins folder)?");
            return false;
        }
        return true;
    }
}
