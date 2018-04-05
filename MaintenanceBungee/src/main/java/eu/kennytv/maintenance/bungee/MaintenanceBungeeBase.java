package eu.kennytv.maintenance.bungee;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

/**
 * @author KennyTV
 * @since 2.1
 */
public final class MaintenanceBungeeBase extends Plugin {
    private MaintenanceBungeePlugin plugin;

    @Override
    public void onEnable() {
        plugin = new MaintenanceBungeePlugin(this);
    }

    @Override
    public void onDisable() {
        plugin.onDisable();
    }

    File getPluginFile() {
        return getFile();
    }
}
