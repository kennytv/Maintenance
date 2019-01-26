package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.IMaintenanceBase;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MaintenanceSpigotBase extends JavaPlugin implements IMaintenanceBase {
    private IMaintenance maintenance;

    @Override
    public void onEnable() {
        maintenance = new MaintenanceSpigotPlugin(this);
    }

    @Override
    public IMaintenance getApi() {
        return maintenance;
    }

    File getPluginFile() {
        return getFile();
    }
}
