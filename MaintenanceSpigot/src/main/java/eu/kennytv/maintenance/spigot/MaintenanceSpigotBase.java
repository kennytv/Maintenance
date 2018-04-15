package eu.kennytv.maintenance.spigot;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author KennyTV
 * @since 2.1
 */
public final class MaintenanceSpigotBase extends JavaPlugin {

    @Override
    public void onEnable() {
        new MaintenanceSpigotPlugin(this);
    }

    File getPluginFile() {
        return getFile();
    }
}
