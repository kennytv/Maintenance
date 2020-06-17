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

package eu.kennytv.maintenance.api.event.manager;

/**
 * Abstract class representing a maintenance eventlistener.
 *
 * @param <T> event to be listened to
 * @author KennyTV
 * @since 3.0.1
 */
public abstract class EventListener<T extends MaintenanceEvent> {

    /**
     * This method is to be overwritten and is called on the given event.
     *
     * @param event fired event
     */
    public abstract void onEvent(final T event);
}
