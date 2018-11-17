package eu.kennytv.maintenance.core.runnable;

import eu.kennytv.maintenance.core.MaintenanceModePlugin;
import eu.kennytv.maintenance.core.Settings;

public abstract class MaintenanceRunnableBase implements Runnable {
    protected final MaintenanceModePlugin plugin;
    protected final Settings settings;
    protected final boolean enable;
    protected int seconds;

    protected MaintenanceRunnableBase(final MaintenanceModePlugin plugin, final Settings settings, final int minutes, final boolean enable) {
        this.plugin = plugin;
        this.settings = settings;
        this.seconds = minutes * 60;
        this.enable = enable;
    }

    @Override
    public void run() {
        if (seconds == 0) {
            finish();
        } else if (settings.getBroadcastIntervalls().contains(seconds)) {
            if (enable)
                plugin.broadcast(startMessageKey());
            else
                plugin.broadcast(endMessageKey());
        }

        seconds--;
    }

    protected abstract void finish();

    protected abstract String startMessageKey();

    protected abstract String endMessageKey();

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
