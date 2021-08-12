/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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
package eu.kennytv.maintenance.api.event;

import eu.kennytv.maintenance.api.event.manager.MaintenanceEvent;

/**
 * Notification event fired when maintenance mode has been changed.
 *
 * @author kennytv
 * @since 3.0.1
 */
public final class MaintenanceChangedEvent implements MaintenanceEvent {
    private final boolean maintenance;

    public MaintenanceChangedEvent(final boolean maintenance) {
        this.maintenance = maintenance;
    }

    /**
     * @return true if maintenance has been enabled, false otherwise
     */
    public boolean isMaintenance() {
        return maintenance;
    }
}
