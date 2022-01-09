/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2022 kennytv (https://github.com/kennytv)
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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Provider to get the maintenance api instance.
 */
public final class MaintenanceProvider {
    private static Maintenance maintenance;

    private MaintenanceProvider() {
        throw new IllegalArgumentException();
    }

    @ApiStatus.Internal
    public static void setMaintenance(final Maintenance maintenance) {
        if (MaintenanceProvider.maintenance != null) {
            throw new IllegalArgumentException("MaintenanceProvider is already set!");
        }
        MaintenanceProvider.maintenance = maintenance;
    }

    /**
     * Returns the maintenance api instance, or null if not loaded yet.
     *
     * @return maintenance api instance
     */
    public static @Nullable Maintenance get() {
        return maintenance;
    }
}
