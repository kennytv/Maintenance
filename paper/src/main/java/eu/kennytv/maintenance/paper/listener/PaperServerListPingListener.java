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
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.paper.MaintenancePaperPlugin;
import eu.kennytv.maintenance.paper.util.ComponentUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PaperServerListPingListener implements Listener {
    private final MaintenancePaperPlugin plugin;
    private final Settings settings;

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
            if (ComponentUtil.PAPER) {
                event.motd(ComponentUtil.toPaperComponent(settings.getRandomPingMessage()));
            } else {
                event.setMotd(ComponentUtil.toLegacy(settings.getRandomPingMessage()));
            }
        }

        if (settings.hasCustomPlayerCountMessage()) {
            event.setProtocolVersion(-1);
            event.setVersion(settings.getLegacyParsedPlayerCountMessage());
        }

        if (settings.hasCustomPlayerCountHoverMessage()) {
            final List<PlayerProfile> sample = event.getPlayerSample();
            sample.clear();
            for (final String string : settings.getLegacyParsedPlayerCountHoverLines()) {
                sample.add(new DummyProfile(string));
            }
        }

        if (settings.hasCustomIcon() && plugin.getFavicon() != null) {
            event.setServerIcon(plugin.getFavicon());
        }
    }

    // Less unnecessary object creation :>
    private static final class DummyProfile implements PlayerProfile {
        private final String name;

        private DummyProfile(final String name) {
            this.name = name;
        }

        @Nullable
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String setName(@Nullable final String name) {
            return this.name;
        }

        @Nullable
        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public UUID setId(@Nullable final UUID uuid) {
            return null;
        }

        @Nonnull
        @Override
        public Set<ProfileProperty> getProperties() {
            return Collections.emptySet();
        }

        @Override
        public boolean hasProperty(final String property) {
            return false;
        }

        @Override
        public void setProperty(final ProfileProperty property) {
        }

        @Override
        public void setProperties(final Collection<ProfileProperty> properties) {
        }

        @Override
        public boolean removeProperty(final String property) {
            return false;
        }

        @Override
        public void clearProperties() {
        }

        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        public boolean completeFromCache() {
            return false;
        }

        @Override
        public boolean completeFromCache(final boolean b) {
            return false;
        }

        @Override
        public boolean completeFromCache(final boolean b, final boolean b1) {
            return false;
        }

        @Override
        public boolean complete(final boolean textures) {
            return false;
        }

        @Override
        public boolean complete(final boolean b, final boolean b1) {
            return false;
        }

        // Keep this here for backwards compatibility
        @SuppressWarnings("unused")
        public GameProfile getGameProfile() {
            return null;
        }
    }
}
