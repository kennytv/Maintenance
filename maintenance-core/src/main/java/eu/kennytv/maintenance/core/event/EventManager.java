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
package eu.kennytv.maintenance.core.event;

import eu.kennytv.maintenance.api.event.manager.EventListener;
import eu.kennytv.maintenance.api.event.manager.IEventManager;
import eu.kennytv.maintenance.api.event.manager.MaintenanceEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventManager implements IEventManager {
    private final Map<String, List<EventListener>> listeners = new HashMap<>();

    @Override
    public void registerListener(final EventListener listener, final Class<? extends MaintenanceEvent> eventClass) {
        listeners.computeIfAbsent(eventClass.getSimpleName(), s -> new ArrayList<>()).add(listener);
    }

    @Override
    public void callEvent(final MaintenanceEvent event) {
        final List<EventListener> list = listeners.get(event.getClass().getSimpleName());
        if (list != null) {
            for (final EventListener listener : list) {
                listener.onEvent(event);
            }
        }
    }
}
