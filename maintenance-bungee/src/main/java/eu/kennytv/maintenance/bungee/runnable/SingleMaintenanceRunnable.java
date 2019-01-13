package eu.kennytv.maintenance.bungee.runnable;

import eu.kennytv.maintenance.api.bungee.IMaintenanceBungee;
import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import net.md_5.bungee.api.config.ServerInfo;

public final class SingleMaintenanceRunnable extends MaintenanceRunnableBase {
    private final ServerInfo server;

    public SingleMaintenanceRunnable(final MaintenanceModePlugin plugin, final Settings settings, final int minutes, final boolean enable, final ServerInfo server) {
        super(plugin, settings, minutes, enable);
        this.server = server;
    }

    @Override
    protected void finish() {
        ((IMaintenanceBungee) this.plugin).setMaintenanceToServer(server, enable);
    }

    @Override
    protected String startMessageKey() {
        return settings.getMessage("singleStarttimerBroadcast").replace("%TIME%", getTime()).replace("%SERVER%", server.getName());
    }

    @Override
    protected String endMessageKey() {
        return settings.getMessage("singleEndtimerBroadcast").replace("%TIME%", getTime()).replace("%SERVER%", server.getName());
    }
}
