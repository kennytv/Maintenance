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

package eu.kennytv.maintenance.spigot;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.IMaintenanceBase;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MaintenanceSpigotBase extends JavaPlugin implements IMaintenanceBase {
    private MaintenancePlugin maintenance;

    @Override
    public void onEnable() {
        maintenance = new MaintenanceSpigotPlugin(this);
    }

    @Override
    public void onDisable() {
        maintenance.disable();
    }

    @Override
    public IMaintenance getApi() {
        return maintenance;
    }

    File getPluginFile() {
        return getFile();
    }
}
