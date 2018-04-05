package eu.kennytv.maintenance.core.runnable;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;

public final class MaintenanceRunnable implements Runnable {
    private final MaintenanceModePlugin plugin;
    private final Settings settings;
    private final boolean enable;
    private int minutes;

    public MaintenanceRunnable(final MaintenanceModePlugin plugin, final Settings settings, final int minutes, final boolean enable) {
        this.plugin = plugin;
        this.settings = settings;
        this.minutes = minutes;
        this.enable = enable;
    }

    @Override
    public void run() {
        if (minutes <= 0) {
            plugin.setMaintenance(enable);

            plugin.cancelTask();
        } else if (settings.getBroadcastIntervalls().contains(minutes)) {
            if (enable)
                plugin.broadcast(settings.getTimerBroadcastMessage().replaceAll("%MINUTES%", String.valueOf(minutes)));
            else
                plugin.broadcast(settings.getEndtimerBroadcastMessage().replaceAll("%MINUTES%", String.valueOf(minutes)));
        }

        minutes--;
    }
}
