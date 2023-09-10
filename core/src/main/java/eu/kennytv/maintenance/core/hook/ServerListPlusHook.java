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
package eu.kennytv.maintenance.core.hook;

//import net.minecrell.serverlistplus.core.ServerListPlusCore;
//import net.minecrell.serverlistplus.core.plugin.ServerListPlusPlugin;

public final class ServerListPlusHook {
    //private final ServerListPlusCore serverListPlusCore;

    public ServerListPlusHook(final Object serverListPlus) {
        //if (!(serverListPlus instanceof ServerListPlusPlugin)) {
        //    throw new IllegalArgumentException("Couldn't parse ServerListPlus instance!");
        //}
        //this.serverListPlusCore = ((ServerListPlusPlugin) serverListPlus).getCore();
    }

    public void setEnabled(final boolean enable) {
        //serverListPlusCore.getProfiles().setEnabled(enable);
    }
}
