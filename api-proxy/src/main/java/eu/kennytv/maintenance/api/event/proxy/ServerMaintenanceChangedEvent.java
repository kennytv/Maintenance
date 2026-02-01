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
package eu.kennytv.maintenance.api.event.proxy;

import eu.kennytv.maintenance.api.event.manager.MaintenanceEvent;
import eu.kennytv.maintenance.api.proxy.Server;

/**
 * Notification event fired when maintenance mode has been changed on a proxied server.
 */
public record ServerMaintenanceChangedEvent(Server server, boolean maintenance) implements MaintenanceEvent {

    /**
     * Returns wrapped server object for the server.
     * <br>
     * This does not necessarily have to be an existing server:
     * If maintenance is disabled on a *previously* registered server, see {@link Server#isRegisteredServer()}.
     *
     * @return wrapped server object for the server
     */
    @Override
    public Server server() {
        return server;
    }

    /**
     * @return true if maintenance has been enabled on the server, false otherwise
     */
    @Override
    public boolean maintenance() {
        return maintenance;
    }
}
