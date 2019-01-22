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

import eu.kennytv.maintenance.core.listener.IPingListener;
import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import eu.kennytv.maintenance.sponge.SettingsSponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public final class ClientPingServerListener implements IPingListener {
    private final MaintenanceSpongePlugin plugin;
    private final SettingsSponge settings;
    private final UUID uuid = new UUID(0, 0);
    private Favicon favicon;

    public ClientPingServerListener(final MaintenanceSpongePlugin plugin, final SettingsSponge settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Listener(order = Order.LAST)
    public void proxyPing(final ClientPingServerEvent event) {
        if (!settings.isMaintenance()) return;

        final ClientPingServerEvent.Response response = event.getResponse();
        if (settings.hasCustomPlayerCountMessage()) {
            //TODO versionmessage possible without much trouble?
        }

        response.setDescription(Text.of(settings.getRandomPingMessage()));
        response.getPlayers().ifPresent(players -> {
            final List<GameProfile> profiles = players.getProfiles();
            profiles.clear();
            for (final String string : settings.getPlayerCountHoverMessage().split("%NEWLINE%"))
                profiles.add(GameProfile.of(uuid, string));
        });

        if (settings.hasCustomIcon() && favicon != null)
            response.setFavicon(favicon);
    }

    @Override
    public boolean loadIcon() {
        try {
            favicon = Sponge.getGame().getRegistry().loadFavicon(ImageIO.read(new File("maintenance-icon.png")));
        } catch (final IOException | IllegalArgumentException e) {
            plugin.getLogger().warning("§4Could not load 'maintenance-icon.png' - did you create one in your Sponge folder (not the plugins folder)?");
            if (settings.debugEnabled())
                e.printStackTrace();
            return false;
        }
        return true;
    }
}
