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
package eu.kennytv.maintenance.core.proxy.runnable;

import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.proxy.MaintenanceProxyPlugin;
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

public final class SingleMaintenanceScheduleRunnable extends SingleMaintenanceRunnable {
    private final int maintenanceDuration;

    public SingleMaintenanceScheduleRunnable(final MaintenancePlugin plugin, final Settings settings, final int seconds, final int maintenanceDuration, final Server server) {
        super(plugin, settings, seconds, true, server);
        this.maintenanceDuration = maintenanceDuration;
    }

    @Override
    protected void finish() {
        super.finish();
        ((MaintenanceProxyPlugin) plugin).startSingleMaintenanceRunnable(server, maintenanceDuration, TimeUnit.SECONDS, false);
    }

    @Override
    protected Component getStartMessage() {
        return settings.getMessage("singleScheduletimerBroadcast",
                "%SERVER%", server.getName(),
                "%TIME%", getTime(), "%DURATION%", plugin.getFormattedTime(maintenanceDuration));
    }
}
