/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018-2020 KennyTV (https://github.com/KennyTV)
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

/**
 * Wrapper for a proxied server.
 * Use {@link IMaintenanceProxy#getServer(String)} to get a server instance.
 *
 * @author KennyTV
 * @since 3.0
 */
public interface Server {

    /**
     * @return name of the server
     */
    String getName();

    /**
     * @return true if there are players on the server
     */
    boolean hasPlayers();

    /**
     * Sends a message to all players on the server.
     *
     * @param message message to be broadcasted
     */
    void broadcast(String message);

    /**
     * Returns whether the server is registered under the proxy.
     * This will only ever be false if maintenance is disabled on a *previously* registered server.
     *
     * @return whether the server is registered under the proxy
     */
    boolean isRegisteredServer();
}
