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

package eu.kennytv.maintenance.api.bungee;

import eu.kennytv.maintenance.api.IMaintenance;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * @author KennyTV
 * @since 2.5
 */
public interface IMaintenanceBungee extends IMaintenance {

    /**
     * Enables/disables maintenance mode on a proxied server.
     * If enabled, all non-permitted players will be kicked.
     * If MySQL is enabled, it will also be written into the database.
     *
     * @param maintenance true to enable, false to disable maintenance mode
     * @return true if the mode was changed
     */
    boolean setMaintenanceToServer(ServerInfo server, boolean maintenance);

    /**
     * @return true if maintenance is currently enabled on the proxied server
     */
    boolean isMaintenance(ServerInfo server);

    /**
     * @return true if a start- or endtimer task is currently running for the proxied server
     */
    boolean isServerTaskRunning(ServerInfo server);
}
