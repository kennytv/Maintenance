/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.kennytv.maintenance.core.runnable;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.util.Task;

public abstract class MaintenanceRunnableBase implements Runnable {
    protected final MaintenancePlugin plugin;
    protected final Settings settings;
    private final Task task;
    protected boolean enable;
    protected int seconds;

    protected MaintenanceRunnableBase(final MaintenancePlugin plugin, final Settings settings, final int seconds, final boolean enable) {
        this.plugin = plugin;
        this.settings = settings;
        this.seconds = seconds;
        this.enable = enable;
        this.task = plugin.startMaintenanceRunnable(this);
    }

    @Override
    public void run() {
        if (seconds == 0) {
            finish();
        } else if (settings.getBroadcastIntervals().contains(seconds)) {
            broadcast(enable ? getStartMessage() : getEndMessage());
        }

        seconds--;
    }

    public String getTime() {
        return plugin.getFormattedTime(seconds);
    }

    public boolean shouldEnable() {
        return enable;
    }

    public int getSecondsLeft() {
        return seconds;
    }

    public Task getTask() {
        return task;
    }

    protected void broadcast(final String message) {
        plugin.broadcast(message);
    }

    protected abstract void finish();

    protected abstract String getStartMessage();

    protected abstract String getEndMessage();
}
