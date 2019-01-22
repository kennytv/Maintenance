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

package eu.kennytv.maintenance.sponge.listener;

import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;

public final class GameReloadListener {
    private final MaintenanceSpongePlugin plugin;

    public GameReloadListener(final MaintenanceSpongePlugin plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void reload(final GameReloadEvent event) {
        plugin.getSettings().reloadConfigs();
        plugin.getLogger().info("Reloaded config files!");
    }
}
