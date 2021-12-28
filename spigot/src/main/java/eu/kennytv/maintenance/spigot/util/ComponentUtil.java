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
package eu.kennytv.maintenance.spigot.util;

import com.google.gson.JsonElement;
import eu.kennytv.maintenance.lib.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public final class ComponentUtil {

    public static final boolean PAPER = isPaper();

    public static Component toPaperComponent(final eu.kennytv.maintenance.lib.kyori.adventure.text.Component component) {
        final JsonElement json = eu.kennytv.maintenance.lib.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serializeToTree(component);
        return GsonComponentSerializer.gson().deserializeFromTree(json);
    }

    public static String toLegacy(final eu.kennytv.maintenance.lib.kyori.adventure.text.Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    private static boolean isPaper() {
        try {
            final Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Class.forName("org.bukkit.entity.Player").getDeclaredMethod("kick", componentClass);
            return true;
        } catch (final NoSuchMethodException | ClassNotFoundException e) {
            return false;
        }
    }
}
