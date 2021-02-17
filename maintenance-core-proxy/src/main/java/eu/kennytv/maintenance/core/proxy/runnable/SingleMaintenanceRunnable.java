/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2021 KennyTV (https://github.com/KennyTV)
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

import eu.kennytv.maintenance.api.proxy.IMaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;

public class SingleMaintenanceRunnable extends MaintenanceRunnableBase {
    protected final Server server;

    public SingleMaintenanceRunnable(final MaintenancePlugin plugin, final Settings settings, final int seconds, final boolean enable, final Server server) {
        super(plugin, settings, seconds, enable);
        this.server = server;
    }

    @Override
    protected void broadcast(final String message) {
        server.broadcast(message);
    }

    @Override
    protected void finish() {
        ((IMaintenanceProxy) plugin).setMaintenanceToServer(server, enable);
    }

    @Override
    protected String getStartMessage() {
        return settings.getMessage("singleStarttimerBroadcast").replace("%TIME%", getTime()).replace("%SERVER%", server.getName());
    }

    @Override
    protected String getEndMessage() {
        return settings.getMessage("singleEndtimerBroadcast").replace("%TIME%", getTime()).replace("%SERVER%", server.getName());
    }
}
