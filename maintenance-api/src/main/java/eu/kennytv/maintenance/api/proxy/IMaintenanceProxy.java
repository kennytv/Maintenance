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

package eu.kennytv.maintenance.api.proxy;

import eu.kennytv.maintenance.api.IMaintenance;

import java.util.Set;

/**
 * @author KennyTV
 * @since 3.0
 */
public interface IMaintenanceProxy extends IMaintenance {

    /**
     * Enables/disables maintenance mode on a proxied server.
     * If enabled, all non-permitted players will be kicked.
     * If MySQL is enabled, it will also be written into the database.
     *
     * @param server      server to apply the maintenance status to
     * @param maintenance true to enable, false to disable maintenance mode
     * @return true if the mode was changed
     * @see #getServer(String)
     */
    boolean setMaintenanceToServer(Server server, boolean maintenance);

    /**
     * @param server server to check
     * @return true if maintenance is currently enabled on the proxied server
     * @see #getServer(String)
     */
    boolean isMaintenance(Server server);

    /**
     * @param server server to check
     * @return true if a start- or endtimer task is currently running for the proxied server
     * @see #getServer(String)
     */
    boolean isServerTaskRunning(Server server);

    /**
     * Returns a wrapped {@link Server} object of a proxied server.
     *
     * @param server name of the proxied server
     * @return wrapped server object if present, else null
     */
    Server getServer(String server);

    /**
     * @return immutable set with names of all proxied server that are currently under maintenance
     */
    Set<String> getMaintenanceServers();
}
