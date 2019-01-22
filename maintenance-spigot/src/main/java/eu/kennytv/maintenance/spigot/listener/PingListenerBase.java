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

package eu.kennytv.maintenance.spigot.listener;

import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotBase;
import eu.kennytv.maintenance.spigot.SettingsSpigot;
import org.bukkit.event.Listener;
import org.bukkit.util.CachedServerIcon;

import javax.imageio.ImageIO;
import java.io.File;

public abstract class PingListenerBase implements IPingListener, Listener {
    protected final MaintenanceSpigotBase plugin;
    protected final SettingsSpigot settings;
    protected CachedServerIcon serverIcon;

    protected PingListenerBase(final MaintenanceSpigotBase plugin, final SettingsSpigot settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public boolean loadIcon() {
        try {
            serverIcon = plugin.getServer().loadServerIcon(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final Exception e) {
            plugin.getLogger().warning("Could not load 'maintenance-icon.png' - did you create one in your Spigot folder (not the plugins folder)?");
            if (settings.debugEnabled())
                e.printStackTrace();
            return false;
        }
        return true;
    }
}
