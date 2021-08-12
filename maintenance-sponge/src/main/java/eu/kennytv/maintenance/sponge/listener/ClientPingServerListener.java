/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2021 kennytv (https://github.com/kennytv)
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

import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.sponge.MaintenanceSpongePlugin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.UUID;

public final class ClientPingServerListener {
    private final UUID uuid = new UUID(0, 0);
    private final MaintenanceSpongePlugin plugin;
    private final Settings settings;

    public ClientPingServerListener(final MaintenanceSpongePlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Listener(order = Order.LAST)
    public void proxyPing(final ClientPingServerEvent event) {
        if (!settings.isMaintenance() || !settings.isEnablePingMessages()) return;

        final ClientPingServerEvent.Response response = event.getResponse();
        if (settings.hasCustomPlayerCountMessage()) {
            //TODO versionmessage possible without much trouble?
            // (spoiler: no, it isn't)
            response.getPlayers().ifPresent(players -> players.setMax(0));
        }

        response.setDescription(Text.of(settings.getRandomPingMessage()));
        response.getPlayers().ifPresent(players -> {
            final List<GameProfile> profiles = players.getProfiles();
            profiles.clear();
            for (final String string : settings.getPlayerCountHoverMessage().split("\n")) {
                profiles.add(GameProfile.of(uuid, string));
            }
        });

        if (settings.hasCustomIcon() && plugin.getFavicon() != null) {
            response.setFavicon(plugin.getFavicon());
        }
    }
}
