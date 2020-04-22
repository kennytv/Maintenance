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

package eu.kennytv.maintenance.api.sponge;

import eu.kennytv.maintenance.api.IMaintenance;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

/**
 * Utility class to get the {@link IMaintenance} instance for the Sponge version of the plugin.
 * <p>
 * Only access this class if you're running the plugin on a Sponge server!
 * </p>
 *
 * @author KennyTV
 * @since 3.0
 */
public final class MaintenanceSpongeAPI {

    /**
     * Returns API instance of the Maintenance plugin.
     *
     * @return {@link IMaintenance} instance
     * @throws IllegalArgumentException if using a custom (or broken?) version of the plugin, that can't be identified
     */
    public static IMaintenance getAPI() {
        final Optional<PluginContainer> container = Sponge.getPluginManager().getPlugin("maintenance");
        if (!container.isPresent() || !container.get().getInstance().isPresent())
            throw new IllegalArgumentException("Could not get instance of Maintenance! Broken/custom build of the plugin?");
        return (IMaintenance) container.get().getInstance().get();
    }
}
