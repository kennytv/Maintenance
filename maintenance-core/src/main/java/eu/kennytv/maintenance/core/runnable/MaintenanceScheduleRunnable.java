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

import java.util.concurrent.TimeUnit;

public final class MaintenanceScheduleRunnable extends MaintenanceRunnable {
    private final int maintenanceDuration;

    public MaintenanceScheduleRunnable(final MaintenancePlugin plugin, final Settings settings, final int secondsToEnable, final int maintenanceDuration) {
        super(plugin, settings, secondsToEnable, true);
        this.maintenanceDuration = maintenanceDuration;
    }

    @Override
    protected void finish() {
        super.finish();

        // Start the timer to disable maintenance again
        plugin.startMaintenanceRunnable(maintenanceDuration, TimeUnit.SECONDS, false);
    }

    @Override
    protected String getStartMessage() {
        return settings.getMessage("scheduletimerBroadcast")
                .replace("%TIME%", getTime()).replace("%DURATION%", plugin.getFormattedTime(maintenanceDuration));
    }
}
