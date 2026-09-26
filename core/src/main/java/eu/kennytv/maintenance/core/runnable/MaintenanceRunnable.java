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
package eu.kennytv.maintenance.core.runnable;

import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class MaintenanceRunnable extends MaintenanceRunnableBase {
    protected final String mode;

    public MaintenanceRunnable(final MaintenancePlugin plugin, final Settings settings, final int seconds, final boolean enable) {
        this(plugin, settings, seconds, enable, null);
    }

    public MaintenanceRunnable(final MaintenancePlugin plugin, final Settings settings, final int seconds, final boolean enable, @Nullable final String mode) {
        super(plugin, settings, seconds, enable);
        this.mode = mode;
    }

    @Override
    protected void finish() {
        plugin.setMaintenance(enable, mode);
    }

    @Override
    protected Component getStartMessage() {
        return settings.getMessage("starttimerBroadcast", "%TIME%", getTime());
    }

    @Override
    protected Component getEndMessage() {
        return settings.getMessage("endtimerBroadcast", "%TIME%", getTime());
    }
}
