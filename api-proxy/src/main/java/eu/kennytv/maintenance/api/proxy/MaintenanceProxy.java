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
package eu.kennytv.maintenance.api.proxy;

import eu.kennytv.maintenance.api.Maintenance;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public interface MaintenanceProxy extends Maintenance {

    /**
     * Enables/disables maintenance mode on a proxied server.
     * If enabled, all non-permitted players will be kicked.
     * If Redis is enabled, it will also be written into the database.
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
    @Nullable
    Server getServer(String server);

    /**
     * Returns a wrapped {@link Server} object of a proxied server, or a dummy server if not registered.
     *
     * @param serverName name of the proxied server
     * @return wrapped server object, or a dummy if not registered
     * @see Server#isRegisteredServer()
     */
    default Server getServerOrDummy(final String serverName) {
        final Server server = getServer(serverName);
        return server != null ? server : new DummyServer(serverName);
    }

    /**
     * Returns the currently registered proxied servers.
     *
     * @return set of proxied servers
     */
    Set<String> getServers();

    /**
     * @return immutable set with names of all proxied server that are currently under maintenance
     */
    Set<String> getMaintenanceServers();
}
