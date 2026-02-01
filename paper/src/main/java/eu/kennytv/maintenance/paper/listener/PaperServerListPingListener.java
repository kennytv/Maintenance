/*
 * This file is part of Maintenance - https://github.com/kennytv/Maintenance
 * Copyright (C) 2018-2024 kennytv (https://github.com/kennytv)
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
package eu.kennytv.maintenance.paper.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent.ListedPlayerInfo;
import com.destroystokyo.paper.profile.PlayerProfile;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.paper.MaintenancePaperPlugin;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PaperServerListPingListener implements Listener {
    private static final BiConsumer<PaperServerListPingEvent, Settings> PLAYER_LIST_SETTER;
    private final MaintenancePaperPlugin plugin;
    private final Settings settings;

    static {
        PLAYER_LIST_SETTER = hasListedPlayers() ? (event, settings) -> {
            final List<ListedPlayerInfo> playerInfoList = event.getListedPlayers();
            playerInfoList.clear();
            for (final String string : settings.getLegacyParsedPlayerCountHoverLines()) {
                playerInfoList.add(new ListedPlayerInfo(string, UUID.randomUUID()));
            }
        } : (event, settings) -> {
            final List<PlayerProfile> sample = event.getPlayerSample();
            sample.clear();
            for (final String string : settings.getLegacyParsedPlayerCountHoverLines()) {
                sample.add(Bukkit.createProfileExact(UUID.randomUUID(), string));
            }
        };
    }

    private static boolean hasListedPlayers() {
        try {
            PaperServerListPingEvent.class.getDeclaredMethod("getListedPlayers");
            return true;
        } catch (final NoSuchMethodException e) {
            return false;
        }
    }

    public PaperServerListPingListener(final MaintenancePaperPlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverListPing(final PaperServerListPingEvent event) {
        if (!settings.isMaintenance()) {
            return;
        }

        if (settings.isEnablePingMessages()) {
            event.motd(settings.getRandomPingMessage());
        }

        if (settings.hasCustomPlayerCountMessage()) {
            event.setProtocolVersion(-1);
            event.setVersion(settings.getLegacyParsedPlayerCountMessage());
        }

        if (settings.hasCustomPlayerCountHoverMessage()) {
            PLAYER_LIST_SETTER.accept(event, settings);
        }

        if (settings.hasCustomIcon() && plugin.getFavicon() != null) {
            event.setServerIcon(plugin.getFavicon());
        }
    }
}
