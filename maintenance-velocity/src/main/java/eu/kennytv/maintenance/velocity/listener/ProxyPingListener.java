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

package eu.kennytv.maintenance.velocity.listener;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.velocity.MaintenanceVelocityPlugin;
import eu.kennytv.maintenance.velocity.SettingsVelocity;
import net.kyori.text.TextComponent;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class ProxyPingListener implements EventHandler<ProxyPingEvent>, IPingListener {
    private final MaintenanceVelocityPlugin plugin;
    private final SettingsVelocity settings;
    private final UUID uuid = new UUID(0, 0);
    private Favicon favicon;

    public ProxyPingListener(final MaintenanceVelocityPlugin plugin, final SettingsVelocity settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void execute(final ProxyPingEvent event) {
        if (!settings.isMaintenance()) return;

        final ServerPing ping = event.getPing();
        final ServerPing.Builder builder = ping.asBuilder();
        if (settings.hasCustomPlayerCountMessage()) {
            builder.version(new ServerPing.Version(1, settings.getPlayerCountMessage()
                    .replace("%ONLINE%", Integer.toString(builder.getMaximumPlayers()))
                    .replace("%MAX%", Integer.toString(builder.getOnlinePlayers()))));
        }

        final String[] split = settings.getPlayerCountHoverMessage().split("%NEWLINE%");
        final ServerPing.SamplePlayer[] samplePlayers = new ServerPing.SamplePlayer[split.length];
        for (int i = 0; i < split.length; i++) {
            samplePlayers[i] = new ServerPing.SamplePlayer(split[i], uuid);
        }
        builder.description(TextComponent.of(settings.getRandomPingMessage()))
                .onlinePlayers(0)
                .maximumPlayers(0)
                .samplePlayers(samplePlayers);

        if (settings.hasCustomIcon() && favicon != null)
            builder.favicon(favicon);
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
