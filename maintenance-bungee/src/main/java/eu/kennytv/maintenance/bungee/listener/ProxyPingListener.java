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

package eu.kennytv.maintenance.bungee.listener;

import eu.kennytv.maintenance.bungee.MaintenanceBungeeBase;
import eu.kennytv.maintenance.bungee.SettingsBungee;
import eu.kennytv.maintenance.core.listener.IPingListener;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public final class ProxyPingListener implements Listener, IPingListener {
    private final MaintenanceBungeeBase plugin;
    private final SettingsBungee settings;
    private Favicon favicon;

    public ProxyPingListener(final MaintenanceBungeeBase plugin, final SettingsBungee settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void proxyPing(final ProxyPingEvent event) {
        if (!settings.isMaintenance()) return;

        final ServerPing ping = event.getResponse();
        if (settings.hasCustomPlayerCountMessage()) {
            ping.getVersion().setProtocol(1);
            ping.getVersion().setName(settings.getPlayerCountMessage()
                    .replace("%ONLINE%", Integer.toString(ping.getPlayers().getOnline()))
                    .replace("%MAX%", Integer.toString(ping.getPlayers().getMax())));
        }

        ping.setDescription(settings.getRandomPingMessage());
        ping.getPlayers().setOnline(0);
        ping.getPlayers().setMax(0);
        ping.getPlayers().setSample(new ServerPing.PlayerInfo[]{
                new ServerPing.PlayerInfo(settings.getPlayerCountHoverMessage().replace("%NEWLINE%", "\n"), "")
        });

        if (settings.hasCustomIcon() && favicon != null)
            ping.setFavicon(favicon);
    }

    @Override
    public boolean loadIcon() {
        try {
            favicon = Favicon.create(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final IOException | IllegalArgumentException e) {
            plugin.getLogger().warning("§4Could not load 'maintenance-icon.png' - did you create one in your Bungee folder (not the plugins folder)?");
            if (settings.debugEnabled())
                e.printStackTrace();
            return false;
        }
        return true;
    }
}
