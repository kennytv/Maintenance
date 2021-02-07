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

package eu.kennytv.maintenance.api;

import eu.kennytv.maintenance.api.event.manager.IEventManager;

/**
 * @author KennyTV
 * @since 2.1
 */
public interface IMaintenance {

    /**
     * Enables/disables maintenance mode.
     * If enabled, all non-permitted players will be kicked.
     * <p>
     * If using the BungeeCord version and MySQL is enabled,
     * it will also be written into the database.
     * </p>
     *
     * @param maintenance true to enable, false to disable maintenance mode
     */
    void setMaintenance(boolean maintenance);

    /**
     * @return true if maintenance is currently enabled
     */
    boolean isMaintenance();

    /**
     * @return true if a start- or endtimer task is currently running
     */
    boolean isTaskRunning();

    /**
     * @return version of the plugin
     */
    String getVersion();

    /**
     * Returns the {@link ISettings} instance, which gives further insight into settings set in the config files.
     *
     * @return {@link ISettings} instance
     */
    ISettings getSettings();

    /**
     * Returns the {@link IEventManager} instance to register listeners to maintenance events.
     *
     * @return {@link IEventManager} instance
     */
    IEventManager getEventManager();

    /**
     * @return true if debug mode is enabled, currently only implemented on Bungee
     */
    boolean isDebug();

    /**
     * Sets the debug mode.
     *
     * @param debug debug state
     */
    void setDebug(boolean debug);
}
