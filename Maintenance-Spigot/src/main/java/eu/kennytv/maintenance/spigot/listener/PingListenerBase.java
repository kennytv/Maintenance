package eu.kennytv.maintenance.spigot.listener;

import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotBase;
import eu.kennytv.maintenance.spigot.SettingsSpigot;
import org.bukkit.event.Listener;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.io.File;

public abstract class PingListenerBase implements IPingListener, Listener {
    protected final MaintenanceSpigotBase plugin;
    protected final SettingsSpigot settings;
    protected CachedServerIcon serverIcon;

    protected PingListenerBase(final MaintenanceSpigotBase plugin, final SettingsSpigot settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public boolean loadIcon() {
        try {
            serverIcon = plugin.getServer().loadServerIcon(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final Exception e) {
            plugin.getLogger().warning("Could not load 'maintenance-icon.png' - did you create one in your Spigot folder (not the plugins folder)?");
            if (settings.debugEnabled())
                e.printStackTrace();
            return false;
        }
        return true;
    }
}
