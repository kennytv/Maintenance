package eu.kennytv.maintenance.core.runnable;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;

public final class MaintenanceRunnable implements Runnable {
    private final MaintenanceModePlugin plugin;
    private final Settings settings;
    private final boolean enable;
    private int seconds;

    public MaintenanceRunnable(final MaintenanceModePlugin plugin, final Settings settings, final int minutes, final boolean enable) {
        this.plugin = plugin;
        this.settings = settings;
        this.seconds = minutes * 60;
        this.enable = enable;
    }

    @Override
    public void run() {
        if (seconds == 0) {
            plugin.setMaintenance(enable);
            if (plugin.isTaskRunning())
                plugin.cancelTask();
        } else if (settings.getBroadcastIntervalls().contains(seconds)) {
            if (enable)
                plugin.broadcast(settings.getMessage("starttimerBroadcast").replaceAll("%TIME%", getTime()));
            else
                plugin.broadcast(settings.getMessage("endtimerBroadcast").replaceAll("%TIME%", getTime()));
        }

        seconds--;
    }

    public String getTime() {
        final int preHours = this.seconds / 60;
        final int minutes = preHours % 60;
        final int seconds = this.seconds % 60;

        final StringBuilder buider = new StringBuilder();
        append(buider, "hour", preHours / 60);
        append(buider, "minute", minutes);
        append(buider, "second", seconds);
        return buider.toString();
    }

    private void append(final StringBuilder builder, final String timeUnit, final int time) {
        if (time == 0) return;
        if (builder.length() != 0)
            builder.append(" ");
        builder.append(time).append(" ").append(settings.getMessage(time == 1 ? timeUnit : timeUnit + "s"));
    }

    public int getSecondsLeft() {
        return seconds;
    }
}
