package eu.kennytv.maintenance.core.runnable;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;

public final class MaintenanceRunnable extends MaintenanceRunnableBase {

    public MaintenanceRunnable(final MaintenanceModePlugin plugin, final Settings settings, final int minutes, final boolean enable) {
        super(plugin, settings, minutes, enable);
    }

    @Override
    protected void finish() {
        plugin.setMaintenance(enable);
        if (plugin.isTaskRunning())
            plugin.cancelTask();
    }

    @Override
    protected String startMessageKey() {
        return settings.getMessage("starttimerBroadcast").replace("%TIME%", getTime());
    }

    @Override
    protected String endMessageKey() {
        return settings.getMessage("endtimerBroadcast").replace("%TIME%", getTime());
    }
}
