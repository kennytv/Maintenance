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

package eu.kennytv.maintenance.sponge;

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.sponge.listener.ClientPingServerListener;
import org.spongepowered.api.Sponge;

public final class SettingsSponge extends Settings {

    SettingsSponge(final MaintenanceSpongePlugin plugin) {
        super(plugin);

        final ClientPingServerListener listener = new ClientPingServerListener(plugin, this);
        Sponge.getEventManager().registerListeners(plugin, listener);
        pingListener = listener;

        reloadConfigs();
    }

    @Override
    protected String getConfigName() {
        return "spigot-config.yml";
    }
}
