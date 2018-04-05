package eu.kennytv.maintenance.spigot;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author KennyTV
 * @since 2.1
 */
public final class MaintenanceSpigotBase extends JavaPlugin {
    private MaintenanceSpigotPlugin plugin;

    @Override
    public void onEnable() {
        plugin = new MaintenanceSpigotPlugin(this);
    }

    @Override
    public void onDisable() {
        plugin.onDisable();
    }

    File getPluginFile() {
        return getFile();
    }
}
