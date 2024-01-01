/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
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
import eu.kennytv.maintenance.lib.kyori.adventure.text.Component;

public class MaintenanceRunnable extends MaintenanceRunnableBase {

    public MaintenanceRunnable(final MaintenancePlugin plugin, final Settings settings, final int seconds, final boolean enable) {
        super(plugin, settings, seconds, enable);
    }

    @Override
    protected void finish() {
        plugin.setMaintenance(enable);
    }

    @Override
    protected Component getStartMessage() {
        return settings.getMessage("starttimerBroadcast", "%TIME%", getTime());
    }

    @Override
    protected Component getEndMessage() {
        return settings.getMessage("endtimerBroadcast", "%TIME%", getTime());
    }
}
