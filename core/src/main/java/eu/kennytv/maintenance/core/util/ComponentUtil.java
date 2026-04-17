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
package eu.kennytv.maintenance.core.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import net.kyori.adventure.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentUtil {

    private static final MethodHandle LEGACY_CLICK_EVENT = legacyClickEvent();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ClickEvent clickEvent(final ClickEvent.Action action, final String value) {
        if (LEGACY_CLICK_EVENT != null) {
            try {
                return (ClickEvent) LEGACY_CLICK_EVENT.invoke(action, value);
            } catch (final Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return ClickEvent.clickEvent(action, ClickEvent.Payload.string(value));
    }

    private static @Nullable MethodHandle legacyClickEvent() {
        try {
            return MethodHandles.publicLookup().unreflect(ClickEvent.class.getDeclaredMethod("clickEvent", ClickEvent.Action.class, String.class));
        } catch (final NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
    }
}
