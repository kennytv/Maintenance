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

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import eu.kennytv.maintenance.core.Settings;
import eu.kennytv.maintenance.spigot.MaintenanceSpigotPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class PaperServerListPingListener implements Listener {
    private final MaintenanceSpigotPlugin plugin;
    private final Settings settings;

    public PaperServerListPingListener(final MaintenanceSpigotPlugin plugin, final Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void serverListPing(final PaperServerListPingEvent event) {
        if (!settings.isMaintenance()) return;

        event.setMotd(settings.getRandomPingMessage());

        if (settings.hasCustomPlayerCountMessage()) {
            event.setProtocolVersion(0);
            event.setVersion(settings.getPlayerCountMessage()
                    .replace("%ONLINE%", Integer.toString(plugin.getServer().getOnlinePlayers().size()))
                    .replace("%MAX%", Integer.toString(plugin.getServer().getMaxPlayers())));
        }

        final List<PlayerProfile> sample = event.getPlayerSample();
        sample.clear();
        for (final String string : settings.getPlayerCountHoverMessage().split("%NEWLINE%"))
            sample.add(new FakePlayerProfile(string));

        if (settings.hasCustomIcon() && plugin.getFavicon() != null)
            event.setServerIcon(plugin.getFavicon());
    }

    // I don't want to import the paper impl. + less object creation :>
    private static final class FakePlayerProfile implements PlayerProfile {
        private static final UUID UUID = new UUID(0, 0);
        private final String name;

        private FakePlayerProfile(final String name) {
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
            return UUID;
        }

        @Override
        public UUID setId(@Nullable final UUID uuid) {
            return UUID;
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
        public boolean complete(final boolean textures) {
            return false;
        }

        // Keep this here for backwards compatibility
        public GameProfile getGameProfile() {
            return null;
        }
    }
}
