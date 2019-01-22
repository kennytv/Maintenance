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

import eu.kennytv.maintenance.api.IMaintenanceBase;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * Utility class to get the {@link IMaintenanceBungee} instance for the BungeeCord version of the plugin.
 * <p>
 * Only access this class if you're running the plugin on a BungeeCord server!
 * </p>
 *
 * @author KennyTV
 * @since 2.5
 */
public final class MaintenanceBungeeAPI {

    /**
     * Returns API instance of the Maintenance plugin.
     *
     * @return {@link IMaintenanceBungee} instance
     * @throws IllegalArgumentException if using a custom (or broken?) version of the plugin, that can't be identified
     */
    public static IMaintenanceBungee getAPI() {
        final Plugin maintenance = ProxyServer.getInstance().getPluginManager().getPlugin("MaintenanceBungee");
        if (maintenance == null)
            throw new IllegalArgumentException("Could not get instance of MaintenanceBungee! Broken/custom build of the plugin?");
        return (IMaintenanceBungee) ((IMaintenanceBase) maintenance).getApi();
    }
}
