/*
 * Maintenance - https://git.io/maintenancemode
 * Copyright (C) 2018 KennyTV (https://github.com/KennyTV)
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

package eu.kennytv.maintenance.bungee;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.IMaintenanceBase;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public final class MaintenanceBungeeBase extends Plugin implements IMaintenanceBase {
    private IMaintenance maintenance;

    @Override
    public void onEnable() {
        maintenance = new MaintenanceBungeePlugin(this);
    }

    @Override
    public IMaintenance getApi() {
        return maintenance;
    }

    File getPluginFile() {
        return getFile();
    }
}
