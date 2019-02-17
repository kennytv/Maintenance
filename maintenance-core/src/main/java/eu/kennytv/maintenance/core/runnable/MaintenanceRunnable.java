/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
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

public final class MaintenanceRunnable extends MaintenanceRunnableBase {

    public MaintenanceRunnable(final MaintenancePlugin plugin, final Settings settings, final int minutes, final boolean enable) {
        super(plugin, settings, minutes, enable);
    }

    @Override
    protected void finish() {
        plugin.setMaintenance(enable);
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
