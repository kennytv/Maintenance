package eu.kennytv.maintenance.bungee;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public final class MaintenanceBungeeBase extends Plugin {

    @Override
    public void onEnable() {
        new MaintenanceBungeePlugin(this);
    }

    File getPluginFile() {
        return getFile();
    }
}
