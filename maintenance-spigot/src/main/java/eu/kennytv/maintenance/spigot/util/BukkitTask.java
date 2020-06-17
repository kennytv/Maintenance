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

package eu.kennytv.maintenance.spigot.util;

import eu.kennytv.maintenance.core.util.Task;
import org.bukkit.Bukkit;

public final class BukkitTask implements Task {
    private final int id;

    public BukkitTask(final int id) {
        this.id = id;
    }

    @Override
    public void cancel() {
        Bukkit.getScheduler().cancelTask(id);
    }
}
