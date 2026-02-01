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
package eu.kennytv.maintenance.core.proxy.runnable;

import eu.kennytv.maintenance.api.proxy.MaintenanceProxy;
import eu.kennytv.maintenance.api.proxy.Server;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.core.runnable.MaintenanceRunnableBase;
import net.kyori.adventure.text.Component;

public class SingleMaintenanceRunnable extends MaintenanceRunnableBase {
    protected final Server server;

    public SingleMaintenanceRunnable(final MaintenancePlugin plugin, final Settings settings, final int seconds, final boolean enable, final Server server) {
        super(plugin, settings, seconds, enable);
        this.server = server;
    }

    @Override
    protected void broadcast(final Component component) {
        server.broadcast(component);
    }

    @Override
    protected void finish() {
        ((MaintenanceProxy) plugin).setMaintenanceToServer(server, enable);
    }

    @Override
    protected Component getStartMessage() {
        return settings.getMessage("singleStarttimerBroadcast",
                "%TIME%", getTime(),
                "%SERVER%", server.name());
    }

    @Override
    protected Component getEndMessage() {
        return settings.getMessage("singleEndtimerBroadcast",
                "%TIME%", getTime(),
                "%SERVER%", server.name());
    }
}
