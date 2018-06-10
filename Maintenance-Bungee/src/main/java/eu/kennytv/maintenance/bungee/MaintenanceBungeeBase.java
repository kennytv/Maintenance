package eu.kennytv.maintenance.bungee;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.IMaintenanceBase;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public final class MaintenanceBungeeBase extends Plugin implements IMaintenanceBase {
    private IMaintenance maintenance;

    @Override
    public void onEnable() {
        maintenance = new MaintenanceBungeePlugin(this);
    }

    @Override
    public IMaintenance getApi() {
        return maintenance;
    }

    File getPluginFile() {
        return getFile();
    }
}
